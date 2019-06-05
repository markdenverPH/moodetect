package com.example.moodetect2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;

import es.dmoral.toasty.Toasty;

public class Global {

    Context context;

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

    public void remove_basic_info(){
        SharedPreferences sharedPref = context.getSharedPreferences("basic_info",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().commit();
    }
}
