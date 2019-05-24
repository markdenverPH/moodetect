package com.example.moodetect2;

import android.animation.Animator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout ll_right, ll_left;
    DisplayMetrics metrics;
    int width_pixels, half_width, one_fourth_width;
    boolean open_left, open_right;

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

        width_pixels = metrics.widthPixels;
        half_width = width_pixels/2;
        one_fourth_width = half_width / 2;

        ll_right.setTranslationX(half_width);
        ll_left.setTranslationX(-half_width);

//        new AnalyzeTextAsyncTask().execute();
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
