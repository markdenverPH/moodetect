package com.example.moodetect2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;

import es.dmoral.toasty.Toasty;

public class Global {

    Context context;
    final static String KEY = "aQiiK9GaQglMl4L8W-o9dYDvQdC_s-_vQWxpaPcYthyJ";

    public Global(Context context) {
        this.context = context;
    }

    public void error_occured(Task task){
        // src: https://stackoverflow.com/questions/14343903/what-is-the-equivalent-of-androidfontfamily-sans-serif-light-in-java-code
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(context, R.font.varela_round))
                .setTextSize(14)
                .apply();
        Toasty.error(context, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();
    }

    public void error_occured(String string){
        // src: https://stackoverflow.com/questions/14343903/what-is-the-equivalent-of-androidfontfamily-sans-serif-light-in-java-code
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(context, R.font.varela_round))
                .setTextSize(14)
                .apply();
        Toasty.error(context, string, Toast.LENGTH_LONG, true).show();
    }

    public void remove_basic_info(){
        SharedPreferences sharedPref = context.getSharedPreferences("basic_info",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().commit();
    }

    public ToneAnalyzer get_analyzer(){
        IamOptions options = new IamOptions.Builder()
                .apiKey(this.KEY)
                .build();
        ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2017-09-21", options);
        toneAnalyzer.setEndPoint("https://gateway.watsonplatform.net/tone-analyzer/api");

        return toneAnalyzer;
    }

    public Typeface getCustomTypeface(){
        return ResourcesCompat.getFont(context, R.font.varela_round);
    }
}
