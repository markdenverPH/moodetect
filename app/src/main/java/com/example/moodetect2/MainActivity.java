package com.example.moodetect2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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
    SwipeRefreshLayout srl_swipe;

    // moodlets
    PieChart pc_moodlets_result;
    LinearLayout ll_base_moodlets, ll_moodlets_content, ll_inner_separator,
            ll_divier;
    TextView tv_header_time_ago, tv_header_emotion, tv_header_input_text,
            tv_suggest_title, tv_suggest_value, tv_input_text, tv_date_time,
            tv_anger_val, tv_fear_val, tv_joy_val, tv_sadness_val;

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
        srl_swipe = findViewById(R.id.srl_swipe);

        // moodlets content initialization
        pc_moodlets_result = findViewById(R.id.pc_moodlets_result);
        ll_moodlets_content = findViewById(R.id.ll_moodlets_content);
        ll_base_moodlets = findViewById(R.id.ll_base_moodlets);
        tv_header_time_ago = findViewById(R.id.tv_header_time_ago);
        tv_header_emotion = findViewById(R.id.tv_header_emotion);
        tv_header_input_text = findViewById(R.id.tv_header_input_text);
        ll_inner_separator = findViewById(R.id.ll_inner_separator);
        tv_suggest_title = findViewById(R.id.tv_suggest_title);
        tv_suggest_value = findViewById(R.id.tv_suggest_value);
        tv_input_text = findViewById(R.id.tv_input_text);
        tv_anger_val = findViewById(R.id.tv_anger_val);
        tv_fear_val = findViewById(R.id.tv_fear_val);
        tv_joy_val = findViewById(R.id.tv_joy_val);
        tv_sadness_val = findViewById(R.id.tv_sadness_val);
        tv_date_time = findViewById(R.id.tv_date_time);
        // end moodlets

        sharedPref = getApplicationContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        last_clicked = 0; // if 1 - speak up, if 2 write up

        String str_temp = sharedPref.getString("bi_first_name", "no first_name") + " " +
                sharedPref.getString("bi_last_name", "no last_name");
        tv_profile_fullname.setText(str_temp);
        str_temp = "How are you, " + sharedPref.getString("bi_first_name", "") + "?";
        tv_greet.setText(str_temp);

        prompt_permission();
        initialize_piechart();

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

        srl_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                show_moodlets();
            }
        });
    }

    private void show_moodlets() {
        if(!srl_swipe.isRefreshing()) {
            srl_swipe.setRefreshing(true);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String uid = user.getUid();

            CollectionReference questionRef = FirebaseFirestore.getInstance().collection("users/"+uid+"/moodlets");
            questionRef.orderBy("int_time", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                        ll_empty_moodlets.setVisibility(View.GONE);
                        ll_last_modlets.setVisibility(View.VISIBLE);

                        MoodletsModel model = task.getResult().toObjects(MoodletsModel.class).get(0);
                        set_suggestion(model);
                        set_emotion(model);
                        set_input_text(model);
                        set_pie_chart(model);
                        set_date_time(model);
                    }
                    srl_swipe.setRefreshing(false);
                }
            });
        }
    }

    private void set_date_time(MoodletsModel model) {
        tv_date_time.setText(model.getDate_time());
        tv_header_time_ago.setText(TimeAgo.getTimeAgo(model.getInt_time()));
    }

    private void set_pie_chart(MoodletsModel model) {
        tv_anger_val.setText("0.0");
        tv_fear_val.setText("0.0");
        tv_joy_val.setText("0.0");
        tv_sadness_val.setText("0.0");
        ArrayList<PieEntry> entries = new ArrayList<>();
        List<String> emo_vals = model.getEmotion_vals();
        List<String> emo_titles = model.getEmotion_titles();
        // sets default value for colors which is black
        int[] colors = {ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e")};

        for(int i = 0; emo_vals.size() > i; i++){
            float temp_hold_float = Float.parseFloat(emo_vals.get(i));
            entries.add(new PieEntry(temp_hold_float, ""));
            String temp_hold_title = emo_titles.get(i);
            switch(temp_hold_title){
                case "anger":
                    colors[i] = ColorTemplate.rgb("#e74c3c");
                    tv_anger_val.setText(String.valueOf(temp_hold_float));
                    break;
                case "fear":
                    colors[i] = ColorTemplate.rgb("#f1c40f");
                    tv_fear_val.setText(String.valueOf(temp_hold_float));
                    break;
                case "joy":
                    colors[i] = ColorTemplate.rgb("#2ecc71");
                    tv_joy_val.setText(String.valueOf(temp_hold_float));
                    break;
                case "sadness":
                    colors[i] = ColorTemplate.rgb("#e67e22");
                    tv_sadness_val.setText(String.valueOf(temp_hold_float));
                    break;
            }
        }
        PieDataSet dataset = new PieDataSet(entries, "Emotion/s");
        dataset.setValueFormatter(new PercentFormatter());    // makes the perc
        dataset.setColors(colors);    // changes the sets of colors
        dataset.setValueTypeface(global.getCustomTypeface()); // typeface for piechart labels (value nad label)
        dataset.setValueTextSize(10f);                  // for value only (percentage value)
        PieData data = new PieData(dataset);
        data.setValueTextColor(Color.BLACK);

        pc_moodlets_result.setData(data);
        pc_moodlets_result.animateY(1000, Easing.EaseInOutCubic); // animate also applies invalidate
    }

    private void set_input_text(MoodletsModel model) {
        tv_header_input_text.setText(model.getInput_text());
        tv_input_text.setText(model.getInput_text());
    }

    private void set_emotion(MoodletsModel model) {
        String str = model.getSugg_emotion().substring(0,1).toUpperCase() + model.getSugg_emotion().substring(1);
        tv_header_emotion.setText(str);
    }

    private void set_suggestion(MoodletsModel model) {
        Resources res = getResources();
        int[] hold_array_id = {0,0};
        switch(model.getSugg_emotion()){
            case "anger":
                hold_array_id[0] = R.array.arr_sugg_titles_anger;
                hold_array_id[1] = R.array.arr_sugg_values_anger;
                break;
            case "fear":
                hold_array_id[0] = R.array.arr_sugg_titles_fear;
                hold_array_id[1] = R.array.arr_sugg_values_fear;
                break;
            case "joy":
                hold_array_id[0] = R.array.arr_sugg_titles_joy;
                hold_array_id[1] = R.array.arr_sugg_values_joy;
                break;
            case "sadness":
                hold_array_id[0] = R.array.arr_sugg_titles_sad;
                hold_array_id[1] = R.array.arr_sugg_values_sad;
                break;
        }
        int sugg_index = model.getSugg_index();
        tv_suggest_title.setText(res.getStringArray(hold_array_id[0])[sugg_index]);
        tv_suggest_value.setText(res.getStringArray(hold_array_id[1])[sugg_index]);
    }

    private void initialize_piechart(){
        pc_moodlets_result.setUsePercentValues(true);    // makes the perc
        pc_moodlets_result.getDescription().setEnabled(false);

        // when the initialized chart is empty
        pc_moodlets_result.setNoDataText("No data.");
        pc_moodlets_result.setNoDataTextColor(R.color.colorPrimaryDark);
        pc_moodlets_result.setNoDataTextTypeface(global.getCustomTypeface());

        pc_moodlets_result.getLegend().setEnabled(false);
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
    protected void onStart() {
        super.onStart();
        // triggers if activity get backed
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
