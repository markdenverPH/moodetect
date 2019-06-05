package com.example.moodetect2;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    LinearLayout ll_right, ll_left;
    DisplayMetrics metrics;
    int width_pixels, half_width, one_fourth_width;
    boolean open_left, open_right;
    Button btn_logout;
    Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ll_right = findViewById(R.id.ll_right);
        ll_left = findViewById(R.id.ll_left);
        open_left = false;
        open_right = false;
        btn_logout = findViewById(R.id.btn_logout);
        global = new Global(getApplicationContext());

        width_pixels = metrics.widthPixels;
        half_width = width_pixels/2;
        one_fourth_width = half_width / 2;

        ll_right.setTranslationX(half_width);
        ll_left.setTranslationX(-half_width);

//        new AnalyzeTextAsyncTask().execute();

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign out then remove shared prefs
                FirebaseAuth.getInstance().signOut();

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
                // checks the shared prefs
                Log.d("check_on", sharedPref.getString("bi_email", "no email"));
                Log.d("check_on", sharedPref.getString("bi_first_name", "no firs_name"));
                Log.d("check_on", sharedPref.getString("bi_last_name", "no last_name"));
                Log.d("check_on", String.valueOf(sharedPref.getBoolean("bi_first_login", true)));
                Log.d("check_on", sharedPref.getString("bi_uid", "no uid"));

                global.remove_basic_info();
                Log.d("check_on", "SHARED PREF REMOVED");

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();

                // ~~ DONE HERE, DO THE UI OF MAIN ACTIVITY
            }
        });
    }

    private void close_left(){
        open_left = false;
        ll_left.animate().translationX(-half_width).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ll_left.setElevation(6);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(open_left){
                    ll_left.setElevation(6);
                } else {
                    ll_left.setElevation(0);
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {

            }
            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void close_right(){
        open_right = false;
        ll_right.animate().translationX(half_width).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ll_right.setElevation(6);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(open_right){
                    ll_right.setElevation(6);
                } else {
                    ll_right.setElevation(0);
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {

            }
            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void trigger_left(View v){
        if(!open_right){
            if(open_left){ // opened
                close_left();
            } else {    //closed
                open_left = true;
                ll_left.setElevation(6);
                ll_left.animate().translationX(-one_fourth_width).setDuration(500);
            }
        } else {
            close_right();
        }
    }

    public void trigger_right(View v){
        if(!open_left){
            if(open_right){ // opened
                close_right();
            } else {    //closed
                open_right = true;
                ll_right.setElevation(6);
                ll_right.animate().translationX(one_fourth_width).setDuration(500);
            }
        } else {
            close_left();
        }
    }
}
