package com.example.moodetect2;

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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    TextView tv_logout;
    Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_logout = findViewById(R.id.tv_logout);
        global = new Global(getApplicationContext());

//        new AnalyzeTextAsyncTask().execute();

        tv_logout.setOnClickListener(new View.OnClickListener() {
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
}
