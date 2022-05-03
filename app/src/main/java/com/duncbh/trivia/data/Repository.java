package com.duncbh.trivia.data;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.duncbh.trivia.controller.AppController;
import com.duncbh.trivia.model.Question;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    ArrayList<Question> questionArrayList = new ArrayList<Question>();
    String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";

    public List<Question> getQuestions(final AnswerListAsyncResponse callback) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            Question question = new Question(
                                    response.getJSONArray(i).getString(0),
                                    response.getJSONArray(i).getBoolean(1)
                            );

                            questionArrayList.add(question);
                            //Log.d("Repo", "onCreate " + response.getJSONArray(i).get(0));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (null != callback)
                        callback.processFinished(questionArrayList);
                }, error -> {

        });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

        return questionArrayList;
    }
}
