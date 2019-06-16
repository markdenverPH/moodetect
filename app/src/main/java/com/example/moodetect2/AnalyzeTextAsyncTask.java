package com.example.moodetect2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    protected String doInBackground(String... string) {
        if(isNetworkConnected(context)){
            Global global = new Global(null);
            ToneAnalyzer toneAnalyzer = global.get_analyzer();

//        String text = "Team, I know that times are tough! Product "
//                + "sales have been disappointing for the past three "
//                + "quarters. We have a competitive product, but we "
//                + "need to do a better job of selling it!";

            ToneOptions toneOptions = new ToneOptions.Builder()
                    .text(string[0])
                    .build();

            ToneAnalysis toneAnalysis = toneAnalyzer.tone(toneOptions).execute().getResult();
            Log.d("tonel", String.valueOf(toneAnalysis));

            return String.valueOf(toneAnalysis);
        } else {
            return "";
        }
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        callback.onTaskComplete(parseJSON(json));
    }

    private ArrayList<String[]> parseJSON(String json){
        try {
            if(json.isEmpty()){
                new Global(context).error_occured("There is no internet connection.");
                return null;
            }
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONObject("document_tone").getJSONArray("tones");
            ArrayList<String[]> result = new ArrayList<>();

            int json_array_length = jsonArray.length();
            if (json_array_length == 0) { //if empty json
                String[] data = {"1", "neutral", "Neutral"};
                result.add(0, data);
                return result;
            } else {
                int i = 0;
//                boolean[] list_emotions = {false, false, false, false}; // anger, fear, joy, sadness
                while (json_array_length > i) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String hold_tone_id = jsonObject.getString("tone_id");

                    if(!(hold_tone_id.equalsIgnoreCase("tentative") ||
                            hold_tone_id.equalsIgnoreCase("analytical") ||
                            hold_tone_id.equalsIgnoreCase("confident"))){
                        String[] data = {jsonObject.getString("score"), hold_tone_id,
                                jsonObject.getString("tone_name")};
                        result.add(i, data);

                        // checks if already added
//                        if(hold_tone_id.equalsIgnoreCase("anger")) {
//                            list_emotions[0] = true;
//                        } else if(hold_tone_id.equalsIgnoreCase("fear")) {
//                            list_emotions[1] = true;
//                        } else if(hold_tone_id.equalsIgnoreCase("joy")) {
//                            list_emotions[2] = true;
//                        } else if(hold_tone_id.equalsIgnoreCase("sadness")) {
//                            list_emotions[3] = true;
//                        }
                    }
                    i++;
                }

//                if(!list_emotions[0]){
//                    String[] data = {"0", "anger", "Anger"};
//                    result.add(i, data);
//                    i++;
//                }
//                if(!list_emotions[1]){
//                    String[] data = {"0", "fear", "Fear"};
//                    result.add(i, data);
//                    i++;
//                }
//                if(!list_emotions[2]){
//                    String[] data = {"0", "joy", "Joy"};
//                    result.add(i, data);
//                    i++;
//                }
//                if(!list_emotions[3]){
//                    String[] data = {"0", "sadness", "Sadness"};
//                    result.add(i, data);
//                    i++;
//                }

                return result;
            }
        } catch (Exception e) {
            Log.d("tonel", e.getStackTrace()[0].getLineNumber() + e.toString());
            new Global(context).error_occured("An error occured, please try again.");
            return null;
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
