package com.example.moodetect2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    TextView tv_logout, tv_greet;
    Global global;
    LinearLayout ll_speak, ll_write;
    LinearLayout ll_base_layout;
    private int last_clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_logout = findViewById(R.id.tv_logout);
        tv_greet = findViewById(R.id.tv_greet);
        global = new Global(getApplicationContext());
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        String str_greet = "How are you, " + sharedPref.getString("bi_first_name", "") + "?";
        tv_greet.setText(str_greet);
        ll_speak = findViewById(R.id.ll_speak);
        ll_write = findViewById(R.id.ll_write);
        ll_base_layout = findViewById(R.id.base_layout_main);
        last_clicked = 0; // if 1 - speak up, if 2 write up

        prompt_permission();

        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_logout.setEnabled(false);

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

            }
        });

        ll_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt_permission();
                last_clicked = 1;
            }
        });

        ll_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt_permission();
                last_clicked = 2;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void prompt_permission(){
        if(Build.VERSION.SDK_INT >= 23){
            requestPermissions(new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.RECORD_AUDIO}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Snackbar.make(ll_base_layout, "Please accept/allow the permissions.", Snackbar.LENGTH_LONG).show();
        } else {
            // granted
            if(last_clicked == 1){
                Intent intent = new Intent(getApplicationContext(), SpeakActivity.class);
                startActivity(intent);
            } else if (last_clicked == 2) {
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                startActivity(intent);
            }
        }
    }
}
