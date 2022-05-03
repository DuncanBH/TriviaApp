package com.duncbh.trivia;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.duncbh.trivia.data.Repository;
import com.duncbh.trivia.databinding.ActivityMainBinding;
import com.duncbh.trivia.model.Question;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String MESSAGE_ID = "message_prefs";

    String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";
    private ActivityMainBinding binding;
    private int currentQuestionIndex = 0;
    List<Question> questions;

    int score = 0;
    int questionsAnswered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Get questions from API
        questions = new Repository().getQuestions(questionArrayList -> {
            //Log.d("MAIN", "onCreate: " + questionArrayList);
            binding.questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
            updateCounter();
        });


        //Event Listeners
        binding.buttonNext.setOnClickListener(view -> {
            unlockButtons();
            slideInNextQuestion();
            updateQuestion();
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
            //updateQuestion();
        });
        binding.buttonPrev.setOnClickListener(view -> {
            if (currentQuestionIndex > 0) {
                unlockButtons();
                slideInLastQuestion();
                updateQuestion();
                currentQuestionIndex = (currentQuestionIndex - 1) % questions.size();
            }
        });


        binding.buttonTrue.setOnClickListener(view -> {
            updateQuestion();
            checkAnswer(true);
            lockButtons();
        });
        binding.buttonFalse.setOnClickListener(view -> {
            updateQuestion();
            checkAnswer(false);
            lockButtons();
        });
        binding.buttonScore.setOnClickListener(view -> {
            binding.textViewScore.setText(String.format("Score: %d/%d", score, questionsAnswered));
        });

        //Log.d("MAIN", "Reading Stuff");
        //Get info back from disk
        SharedPreferences getSharedData = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);

        score = getSharedData.getInt("score", 0);
        questionsAnswered = getSharedData.getInt("questionsAnswered", 0);
        currentQuestionIndex = getSharedData.getInt("currentQuestionIndex", 0);
        //Log.d("MAIN", "Read: score: " + score + " quesAnswered: " + questionsAnswered + " index: " + currentQuestionIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d("MAIN", "Saving Stuff");
        SharedPreferences sharedPreferences = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("score", score);
        editor.putInt("questionsAnswered", questionsAnswered);
        editor.putInt("currentQuestionIndex", currentQuestionIndex);
        //Log.d("MAIN", "Save: score: " + score + " quesAnswered: " + questionsAnswered + " index: " + currentQuestionIndex);
        editor.apply();
    }

    private void checkAnswer(boolean userAnswer) {
        boolean correctAnswer = questions.get(currentQuestionIndex).isAnswerTrue();

        questionsAnswered++;

        int snackMessageId = 0;
        if (userAnswer == correctAnswer) {
            snackMessageId = R.string.correct_answer;
            fadeAnimation();
            score++;
        } else {
            snackMessageId = R.string.incorrect_answer;
            shakeAnimation();
        }

        Snackbar.make(binding.cardView, snackMessageId, Snackbar.LENGTH_SHORT).show();
    }

    private void updateQuestion() {
        String question = questions.get(currentQuestionIndex).getAnswer();
        binding.questionTextview.setText(question);
        updateCounter();
    }

    private void updateCounter() {
        binding.textViewOutOf.setText(String.format("Question: %d/%d", currentQuestionIndex + 1, questions.size()));
    }

    private void lockButtons() {
        binding.buttonTrue.setEnabled(false);
        binding.buttonFalse.setEnabled(false);
        binding.buttonTrue.setBackgroundColor(Color.BLACK);
        binding.buttonFalse.setBackgroundColor(Color.BLACK);
    }

    private void unlockButtons() {
        binding.buttonTrue.setEnabled(true);
        binding.buttonFalse.setEnabled(true);
        binding.buttonTrue.setBackgroundColor(getResources().getColor(R.color.button_color));
        binding.buttonFalse.setBackgroundColor(getResources().getColor(R.color.button_color));
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        binding.cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void slideInLastQuestion() {
        Animation slideOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_left_animation);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation slideIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_left_animation);
                binding.cardView.setAnimation(slideIn);
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        binding.cardView.setAnimation(slideOut);
    }

    private void slideInNextQuestion() {
        Animation slideOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_right_animation);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation slideIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_right_animation);
                binding.cardView.setAnimation(slideIn);
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        binding.cardView.setAnimation(slideOut);
    }

    private void fadeAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        binding.cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}