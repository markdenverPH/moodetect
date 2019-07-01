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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv_logout, tv_greet, tv_profile_fullname;
    Global global;
    LinearLayout ll_speak, ll_write, ll_moodlets, ll_base_layout, ll_empty_moodlets,
            ll_last_modlets;
    private int last_clicked;
    ImageView iv_profile;
    ProfileDialog profileDialog;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        global = new Global(getApplicationContext());
        profileDialog = new ProfileDialog();
        tv_logout = findViewById(R.id.tv_logout);
        tv_greet = findViewById(R.id.tv_greet);
        ll_speak = findViewById(R.id.ll_speak);
        ll_write = findViewById(R.id.ll_write);
        ll_base_layout = findViewById(R.id.base_layout_main);
        tv_profile_fullname = findViewById(R.id.tv_profile_fullname);
        iv_profile = findViewById(R.id.iv_profile);
        ll_moodlets = findViewById(R.id.ll_moodlets);
        ll_empty_moodlets = findViewById(R.id.ll_empty_moodlets);
        ll_last_modlets = findViewById(R.id.ll_last_modlets);
        sharedPref = getApplicationContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        last_clicked = 0; // if 1 - speak up, if 2 write up

        String str_temp = sharedPref.getString("bi_first_name", "no first_name") + " " +
                sharedPref.getString("bi_last_name", "no last_name");
        tv_profile_fullname.setText(str_temp);
        str_temp = "How are you, " + sharedPref.getString("bi_first_name", "") + "?";
        tv_greet.setText(str_temp);

        prompt_permission();

        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_logout.setEnabled(false);

                // sign out then remove shared prefs
                FirebaseAuth.getInstance().signOut();

                sharedPref = getApplicationContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);

                global.remove_basic_info();

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

        tv_profile_fullname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_profile();
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_profile();
            }
        });

        ll_moodlets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Moodlets.class);
                startActivity(intent);
            }
        });
    }

    private void show_moodlets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        CollectionReference questionRef = FirebaseFirestore.getInstance().collection("users/"+uid+"/moodlets");
        questionRef.orderBy("int_time", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshot) {
                if(!snapshot.isEmpty()){
                    ll_empty_moodlets.setVisibility(View.GONE);
                    ll_last_modlets.setVisibility(View.VISIBLE);
                    MoodletsModel model = snapshot.toObjects(MoodletsModel.class).get(0);
                    // CONITNUE ~~~~ show the last moodlets
                } else {
                    // stay default
                }
            }
        });
    }

    private void show_profile() {
        if(!profileDialog.isVisible()){
            profileDialog.show(getSupportFragmentManager(), "profile_dialog");
        }
    }

    private void prompt_permission(){
        if(Build.VERSION.SDK_INT >= 23){
            requestPermissions(new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.RECORD_AUDIO}, 2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        show_moodlets();
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
        } else if(last_clicked != 0) {
            //granted

            Intent intent = new Intent(getApplicationContext(), SpeakActivity.class);
            intent.putExtra("last_clicked", last_clicked);
            startActivity(intent);
        }
    }
}
