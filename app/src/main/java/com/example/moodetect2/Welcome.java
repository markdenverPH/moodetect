package com.example.moodetect2;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.auth.FirebaseAuth;

public class Welcome extends AppCompatActivity {
    FirebaseAuth auth;
    View logo, text_moodetect;
    Intent main_intent, login_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        auth = FirebaseAuth.getInstance();
        logo = findViewById(R.id.logo);
        text_moodetect = findViewById(R.id.text_moodetect);
        main_intent = new Intent(this, MainActivity.class);
        login_intent = new Intent(Welcome.this, Login.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            startActivity(main_intent);
        } else {
            Animation fade_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            logo.startAnimation(fade_in);
            text_moodetect.startAnimation(fade_in);
            fire_intent();
        }
    }

    private void fire_intent(){
        // delays the opening of acitivty
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(login_intent);
                finish();
            }
        }, 1700);
    }
}
