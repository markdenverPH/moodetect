package com.example.moodetect2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Locale;

public class SpeakActivity extends AppCompatActivity implements AnalyzeTextAsyncTask.AsyncTaskCompleteListener<String> {

    ImageButton ib_mic;
    EditText et_transcript;
    Button btn_clear, btn_process;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Global global;
    TextView tv_transcript, tv_instruction, tv_suggest_title, tv_suggest_value;
    ScrollView sv_speak_up;
    LinearLayout ll_mic_layout, ll_base_layout_speak;
    ActionBar actionBar;
    PieChart pc_result;
    String[] sugg_titles, sugg_values;
    Resources res;
    String str_compare1, str_compare2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak);
        global = new Global(this);
        ib_mic = findViewById(R.id.ib_mic);
        et_transcript = findViewById(R.id.et_transcript);
        btn_clear = findViewById(R.id.btn_speak_clear);
        btn_process = findViewById(R.id.btn_speak_process);
        sv_speak_up = findViewById(R.id.sv_speak_up);
        ll_mic_layout = findViewById(R.id.ll_mic_layout);
        tv_transcript = findViewById(R.id.tv_transcript);
        tv_instruction = findViewById(R.id.tv_instruction);
        pc_result = findViewById(R.id.pc_result);
        ll_base_layout_speak = findViewById(R.id.ll_base_layout_speak);
        tv_suggest_title = findViewById(R.id.tv_suggest_title);
        tv_suggest_value = findViewById(R.id.tv_suggest_value);
        actionBar = getSupportActionBar();
        Intent intent = getIntent();    // get last_clicked, identify what activity
        res = getResources();
        str_compare1 = "";
        str_compare2 = "";
        initialized_pie_chart();

        // https://stackoverflow.com/questions/19207762/must-i-specify-the-parent-activity-name-in-the-android-manifest
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            is_it_write_up(intent.getIntExtra("last_clicked", 0));
        }

        ib_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak up!");
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    global.error_occured("Your device does not support voice input.");
                }
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_transcript.setText("");
            }
        });

        btn_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = String.valueOf(et_transcript.getText());
                str_compare1 = temp;
                if(temp.length() >= 10){
                    if(!str_compare1.equalsIgnoreCase(str_compare2)){
                        disable_button(true);
                        AnalyzeTextAsyncTask analyze = new AnalyzeTextAsyncTask(SpeakActivity.this, getApplicationContext());
                        String[] arr_temp = {temp};
                        analyze.execute(arr_temp);
                        hideSoftKeyboard(SpeakActivity.this);
                    } else {
                        Snackbar.make(ll_base_layout_speak, "Same input is detected. Please change the input.",
                                Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(ll_base_layout_speak, "Text field requires at least 10 characters.",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        et_transcript.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sv_speak_up.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    public void onTaskComplete(ArrayList<String[]> result) {
        if(result == null){
            Log.d("dumaan_dito", "return null from async");
            pc_result.setNoDataText("Data cannot be processed. Please try again.");
        } else {
            float highest_val = 0;
            String highest_emotion = "";

            ArrayList<PieEntry> entries = new ArrayList<>();
            for(int i = 0; result.size() > i; i++){
                float temp_hold_float = Float.parseFloat(result.get(i)[0]);
                entries.add(new PieEntry(temp_hold_float, result.get(i)[2]));

                if(highest_val == 0 || highest_val < temp_hold_float) {
                    highest_val = temp_hold_float;
                    highest_emotion = result.get(i)[2];
                }
            }
            PieDataSet dataset = new PieDataSet(entries, "Emotion/s");
            dataset.setValueFormatter(new PercentFormatter());    // makes the perc
            dataset.setColors(ColorTemplate.JOYFUL_COLORS);    // changes the sets of colors
            dataset.setValueTypeface(global.getCustomTypeface()); // typeface for piechart labels (value nad label)
            dataset.setValueTextSize(14f);                  // for value only (percentage value)
            PieData data = new PieData(dataset);
            data.setValueTextColor(Color.BLACK);

            pc_result.setData(data);
            pc_result.animateY(1000, Easing.EaseInOutCubic); // animate also applies invalidate
//            pc_result.invalidate(); // refresh
            sv_speak_up.fullScroll(ScrollView.FOCUS_DOWN);

            if(result.get(0)[2].equalsIgnoreCase("neutral")) {
                tv_suggest_title.setVisibility(View.GONE);
                tv_suggest_value.setText("None.");
            } else {
                switch(highest_emotion){
                    case "Anger":
                        sugg_titles = res.getStringArray(R.array.arr_sugg_titles_anger);
                        sugg_values = res.getStringArray(R.array.arr_sugg_values_anger);
                        break;
                    case "Fear":
                        sugg_titles = res.getStringArray(R.array.arr_sugg_titles_fear);
                        sugg_values = res.getStringArray(R.array.arr_sugg_values_fear);
                        break;
                    case "Joy":
                        sugg_titles = res.getStringArray(R.array.arr_sugg_titles_joy);
                        sugg_values = res.getStringArray(R.array.arr_sugg_values_joy);
                        break;
                    case "Sadness":
                        sugg_titles = res.getStringArray(R.array.arr_sugg_titles_sad);
                        sugg_values = res.getStringArray(R.array.arr_sugg_values_sad);
                        break;
                }
                tv_suggest_title.setVisibility(View.VISIBLE);
                tv_suggest_title.setText(sugg_titles[0]);
                tv_suggest_value.setText(sugg_values[0]);
            }
        }

        // CONITNUE --- ADD ROWS IN MOODLETS FIRESTORE

        disable_button(false);
        str_compare2 = str_compare1;
    }

    private void initialized_pie_chart() {
        pc_result.setUsePercentValues(true);    // makes the perc
        pc_result.getDescription().setEnabled(false);

        // when the initialized chart is empty
        pc_result.setNoDataText("No data.");
        pc_result.setNoDataTextColor(R.color.colorPrimaryDark);
        pc_result.setNoDataTextTypeface(global.getCustomTypeface());

        pc_result.setEntryLabelTextSize(14f);       // label of value percentage in the piechart itself
        pc_result.setEntryLabelColor(R.color.colorPrimaryDark);
        pc_result.setEntryLabelTypeface(global.getCustomTypeface());

        Legend l = pc_result.getLegend();       // changes the legend auto-gen by ColorTemplate
        l.setTextSize(14f);
        l.setTypeface(global.getCustomTypeface());
    }

    private void is_it_write_up(int clicked) {
        if(clicked == 2){
            ll_mic_layout.setVisibility(View.GONE);
            actionBar.setTitle("Write up");
            tv_transcript.setText("Input");
            tv_instruction.setText("Write below what you feel.\nClick process to proceed.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && null != data){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(!result.get(0).isEmpty()){
                String str_hold = String.valueOf(et_transcript.getText());
                str_hold += " " + result.get(0);
                et_transcript.setText(str_hold);
            } else {
                et_transcript.setText(result.get(0));
            }
        }
    }

    private void disable_button(boolean value){
        if(value){
            ib_mic.setEnabled(false);
            ib_mic.setAlpha(0.4f);
            btn_clear.setEnabled(false);
            btn_clear.setAlpha(0.4f);
            btn_process.setEnabled(false);
            btn_process.setAlpha(0.4f);
        } else {
            ib_mic.setEnabled(true);
            ib_mic.setAlpha(1f);
            btn_clear.setEnabled(true);
            btn_clear.setAlpha(1f);
            btn_process.setEnabled(true);
            btn_process.setAlpha(1f);
        }
    }

    private void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}