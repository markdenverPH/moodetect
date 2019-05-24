package com.example.moodetect2;

import android.os.AsyncTask;
import android.util.Log;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

class AnalyzeTextAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        IamOptions options = new IamOptions.Builder()
                .apiKey("aQiiK9GaQglMl4L8W-o9dYDvQdC_s-_vQWxpaPcYthyJ")
                .build();

        ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2017-09-21", options);
        toneAnalyzer.setEndPoint("https://gateway.watsonplatform.net/tone-analyzer/api");

        String text = "Team, I know that times are tough! Product "
                + "sales have been disappointing for the past three "
                + "quarters. We have a competitive product, but we "
                + "need to do a better job of selling it!";

        ToneOptions toneOptions = new ToneOptions.Builder()
                .text(text)
                .build();

        ToneAnalysis toneAnalysis = toneAnalyzer.tone(toneOptions).execute().getResult();
        Log.d("tonel", String.valueOf(toneAnalysis));
        System.out.println("tonel" + toneAnalysis);

        return null;
    }
}
