package com.example.moodetect2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

class AnalyzeTextAsyncTask extends AsyncTask<String, Void, String> {
    private AsyncTaskCompleteListener callback;
    private Context context;

    public interface AsyncTaskCompleteListener<String> {
        void onTaskComplete(ArrayList<String[]> result);
    }

    //src: https://xelsoft.wordpress.com/2014/11/28/asynctask-implementation-using-callback-interface/
    public AnalyzeTextAsyncTask(AsyncTaskCompleteListener context_interface, Context context){
        this.context = context;
        callback = context_interface;
    }

    @Override
    protected String doInBackground(String... strings) {
        Global global = new Global(null);
        ToneAnalyzer toneAnalyzer = global.get_analyzer();

        String text = "Team, I know that times are tough! Product "
                + "sales have been disappointing for the past three "
                + "quarters. We have a competitive product, but we "
                + "need to do a better job of selling it!";

        ToneOptions toneOptions = new ToneOptions.Builder()
                .text(text)
                .build();

        ToneAnalysis toneAnalysis = toneAnalyzer.tone(toneOptions).execute().getResult();
        Log.d("tonel", String.valueOf(toneAnalysis));

        return String.valueOf(toneAnalysis);
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        callback.onTaskComplete(parseJSON(json));
    }

    private ArrayList<String[]> parseJSON(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONObject("document_tone").getJSONArray("tones");
            ArrayList<String[]> result = new ArrayList<>();

            int json_array_length = jsonArray.length();
            if (json == "" || json_array_length == 0) { //if empty json
                return null;
            } else {
                int i = 0;

                while (json_array_length > i) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String hold_tone_id = jsonObject.getString("tone_id");

                    if(!(hold_tone_id.equalsIgnoreCase("tentative") ||
                            hold_tone_id.equalsIgnoreCase("analytical") ||
                            hold_tone_id.equalsIgnoreCase("confident"))){
                        String[] data = {jsonObject.getString("score"), jsonObject.getString("tone_id"),
                                jsonObject.getString("tone_name")};
                        result.add(i, data);
                    }
                    i++;
                }
                return result;
            }
        } catch (Exception e) {
            Log.i("tonel", e.getStackTrace()[0].getLineNumber() + e.toString());
            new Global(context).error_occured("An error occured, please try again.");
            return null;
        }
    }
}
