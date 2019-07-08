package com.example.moodetect2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    PieData data;
    int index;

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
        index = 0;
        initialized_pie_chart();

        // https://stackoverflow.com/questions/19207762/must-i-specify-the-parent-activity-name-in-the-android-manifest
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            is_it_write_up(intent.getIntExtra("last_clicked", 0));
        }

        // click listener to trigger Android's built-in speech recognition
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

        // clears the input text
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_transcript.setText("");
            }
        });

        // process the input text by passing the string into asynctask class
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

    // when conversion of json object to arraylist is finished,
    // this callback function is triggered and basically insert data to database and
    // display the information in UI
    @Override
    public void onTaskComplete(ArrayList<String[]> result) {
        if(result == null){
            pc_result.setNoDataText("Data cannot be processed. Please try again.");
        } else {
            float highest_val = 0;
            String highest_emotion = "";
            List<String> emo_title = new ArrayList<>();
            List<String> emo_values = new ArrayList<>();
            // sets default value for colors which is black
            int[] colors = {ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e"), ColorTemplate.rgb("#34495e")};

            ArrayList<PieEntry> entries = new ArrayList<>();
            for(int i = 0; result.size() > i; i++){
                float temp_hold_float = Float.parseFloat(result.get(i)[0]) * 100;
                String temp_hold_title = result.get(i)[2]; // gets the upper case first letter
                entries.add(new PieEntry(temp_hold_float, temp_hold_title));

                emo_title.add(result.get(i)[1]); // gets the lower case one
                String round_off = String.format("%.2f", temp_hold_float);
                emo_values.add(round_off);

                switch(temp_hold_title){
                    case "Anger":
                        colors[i] = ColorTemplate.rgb("#e74c3c");
                        break;
                    case "Fear":
                        colors[i] = ColorTemplate.rgb("#f1c40f");
                        break;
                    case "Joy":
                        colors[i] = ColorTemplate.rgb("#2ecc71");
                        break;
                    case "Sadness":
                        colors[i] = ColorTemplate.rgb("#e67e22");
                        break;
                }

                if(highest_val == 0 || highest_val < temp_hold_float) {
                    highest_val = temp_hold_float;
                    highest_emotion = result.get(i)[2];
                }
            }
            PieDataSet dataset = new PieDataSet(entries, "Emotion/s");
            dataset.setValueFormatter(new PercentFormatter());    // makes the perc
            dataset.setColors(colors);    // changes the sets of colors
            dataset.setValueTypeface(global.getCustomTypeface()); // typeface for piechart labels (value nad label)
            dataset.setValueTextSize(14f);                  // for value only (percentage value)
            data = new PieData(dataset);
            data.setValueTextColor(Color.BLACK);

            if(result.get(0)[2].equalsIgnoreCase("neutral")) {
                tv_suggest_title.setVisibility(View.GONE);
                tv_suggest_value.setText("None.");
                pc_result.setData(null);
                pc_result.setNoDataText("Emotion is undetermined.");
                pc_result.invalidate();
                sv_speak_up.fullScroll(ScrollView.FOCUS_DOWN);
            } else {
                int[] hold_array_id = {0,0};
                switch(highest_emotion){
                    case "Anger":
                        hold_array_id[0] = R.array.arr_sugg_titles_anger;
                        hold_array_id[1] = R.array.arr_sugg_values_anger;
                        break;
                    case "Fear":
                        hold_array_id[0] = R.array.arr_sugg_titles_fear;
                        hold_array_id[1] = R.array.arr_sugg_values_fear;
                        break;
                    case "Joy":
                        hold_array_id[0] = R.array.arr_sugg_titles_joy;
                        hold_array_id[1] = R.array.arr_sugg_values_joy;
                        break;
                    case "Sadness":
                        hold_array_id[0] = R.array.arr_sugg_titles_sad;
                        hold_array_id[1] = R.array.arr_sugg_values_sad;
                        break;
                }
                sugg_titles = res.getStringArray(hold_array_id[0]);
                sugg_values = res.getStringArray(hold_array_id[1]);
                tv_suggest_title.setVisibility(View.VISIBLE);
                index = generate_random_number(hold_array_id[0], res);

                // setup insertion
                long time = System.currentTimeMillis();
                DateFormat df = new SimpleDateFormat("MMM d, yyyy h:mm a");
                MoodletsModel model = new MoodletsModel(df.format(new Date(time)), emo_title, emo_values,
                        String.valueOf(et_transcript.getText()), highest_emotion.toLowerCase(), index, time);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String uid = user.getUid();
                CollectionReference questionRef = FirebaseFirestore.getInstance().collection("users/"+uid+"/moodlets");
                questionRef.document(String.valueOf(time)).set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            // insert to firestore first before showing results
                            tv_suggest_title.setText(sugg_titles[index]);
                            tv_suggest_value.setText(sugg_values[index]);
                            pc_result.setData(data);
                            pc_result.animateY(1000, Easing.EaseInOutCubic); // animate also applies invalidate
                            sv_speak_up.fullScroll(ScrollView.FOCUS_DOWN);
                        } else {
                            global.error_occured(task);
                        }
                    }
                });
            }
        }

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

    // check if it is "Write Up" or "Speak Up".
    // basically, "Write Up" is the same as "Speak Up"
    // the mic function is hidden from UI
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

    // callback function from speech recognition
    // gets the result as string
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
        InputMethodManager imm = (InputMethodManager)activity.getSystemService( Context.INPUT_METHOD_SERVICE );
        View f = activity.getCurrentFocus();
        if( null != f && null != f.getWindowToken() && EditText.class.isAssignableFrom( f.getClass() ) )
            imm.hideSoftInputFromWindow( f.getWindowToken(), 0 );
        else
            activity.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
    }

    // generates random number as a selected suggestion from list of suggestions
    private int generate_random_number(int array_id, Resources res){
        // src: https://stackoverflow.com/questions/21049747/how-can-i-generate-a-random-number-in-a-certain-range/21049922

        String[] hold_values = res.getStringArray(array_id);
        int min = 0;
        int max = hold_values.length;
        Random rand = new Random();
        // inclusive of min, exclusive of max
        return rand.nextInt(max - min) + min;
    }
}
