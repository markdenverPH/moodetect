package com.example.moodetect2;

import android.arch.paging.PagedList;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Moodlets extends AppCompatActivity {
    // src: https://www.androidhive.info/2018/01/android-content-placeholder-animation-like-facebook-using-shimmer/
    // src CODE: https://github.com/firebase/FirebaseUI-Android/blob/master/app/src/main/java/com/firebase/uidemo/database/firestore/FirestorePagingActivity.java
    LinearLayout ll_preloader;
    RecyclerView rv_moodlets;
    LinearLayoutManager layoutManager;
    Global global;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moodlets);
        ll_preloader = findViewById(R.id.ll_preloader);
        rv_moodlets = findViewById(R.id.rv_moodlets);
        layoutManager = new LinearLayoutManager(this);
        global = new Global(this);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Removes blinks
        ((SimpleItemAnimator) rv_moodlets.getItemAnimator()).setSupportsChangeAnimations(false);
//        rv_moodlets.setHasFixedSize(true);
        rv_moodlets.setLayoutManager(layoutManager);
        fetch_moodlets();
    }

    private void fetch_moodlets() {
        ll_preloader.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        Query query = FirebaseFirestore.getInstance()
                .collection("users/"+uid+"/moodlets")
                .orderBy("int_time", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(3)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<MoodletsModel> options = new FirestorePagingOptions.Builder<MoodletsModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, MoodletsModel.class)
                .build();

        // firestore do the work on fetching and adding additional rows
        // when the scroll reaches the bottom
        final FirestorePagingAdapter<MoodletsModel, MoodletsViewHolder> adapter =
                new FirestorePagingAdapter<MoodletsModel, MoodletsViewHolder>(options) {
                    View v;
                    @NonNull
                    @Override
                    public MoodletsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        v = LayoutInflater.from(getApplicationContext())
                                .inflate(R.layout.moodlets_row, viewGroup, false);
                        return new MoodletsViewHolder(v);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final MoodletsViewHolder holder, final int i, @NonNull MoodletsModel model) {
                        holder.bind(model);
                        ll_preloader.setVisibility(View.GONE);

                        holder.ll_base_moodlets.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // src: https://medium.com/@nikola.jakshic/how-to-expand-collapse-items-in-recyclerview-49a648a403a6
                                holder.expand_collapse();
                                notifyItemChanged(i);
                            }
                        });
                    }

                    @Override
                    protected void onError(@NonNull Exception e) {
                        global.error_occured("An error occured while fetching the data.");
                    }
                };
        rv_moodlets.setAdapter(adapter);
        rv_moodlets.setHasFixedSize(true);
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
    protected void onResume() {
        super.onResume();
//        shimmer.startShimmer();
        fetch_moodlets();
    }

    private class MoodletsViewHolder extends RecyclerView.ViewHolder {
        PieChart pc_moodlets_result;
        LinearLayout ll_base_moodlets, ll_moodlets_content, ll_inner_separator,
                ll_divider;
        TextView tv_header_time_ago, tv_header_emotion, tv_header_input_text,
                tv_suggest_title, tv_suggest_value, tv_input_text, tv_date_time,
                tv_anger_val, tv_fear_val, tv_joy_val, tv_sadness_val;
        Resources res;
        Global global;

        MoodletsViewHolder(@NonNull View v) {
            super(v);
            pc_moodlets_result = v.findViewById(R.id.pc_moodlets_result);
            ll_moodlets_content = v.findViewById(R.id.ll_moodlets_content);
            ll_base_moodlets = v.findViewById(R.id.ll_base_moodlets);
            tv_header_time_ago = v.findViewById(R.id.tv_header_time_ago);
            tv_header_emotion = v.findViewById(R.id.tv_header_emotion);
            tv_header_input_text = v.findViewById(R.id.tv_header_input_text);
            ll_inner_separator = v.findViewById(R.id.ll_inner_separator);
            tv_suggest_title = v.findViewById(R.id.tv_suggest_title);
            tv_suggest_value = v.findViewById(R.id.tv_suggest_value);
            tv_input_text = v.findViewById(R.id.tv_input_text);
            tv_anger_val = v.findViewById(R.id.tv_anger_val);
            tv_fear_val = v.findViewById(R.id.tv_fear_val);
            tv_joy_val = v.findViewById(R.id.tv_joy_val);
            tv_sadness_val = v.findViewById(R.id.tv_sadness_val);
            tv_date_time = v.findViewById(R.id.tv_date_time);
            ll_divider = v.findViewById(R.id.ll_divider);
            global = new Global(getApplicationContext());
            res = getResources();

            initialize_piechart();
        }

        void bind(@NonNull MoodletsModel model) {
            set_suggestion(model);
            set_emotion(model);
            set_input_text(model);
            set_pie_chart(model);
            set_date_time(model);
        }

        private void set_date_time(MoodletsModel model) {
            tv_date_time.setText(model.getDate_time());
            tv_header_time_ago.setText(TimeAgo.getTimeAgo(model.getInt_time()));
        }

        private void set_pie_chart(MoodletsModel model) {
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
            pc_moodlets_result.animateY(1000, Easing.EaseInOutCubic); // animate also applies invalidate

            pc_moodlets_result.setUsePercentValues(true);    // makes the perc
            pc_moodlets_result.getDescription().setEnabled(false);

            // when the initialized chart is empty
            pc_moodlets_result.setNoDataText("No data.");
            pc_moodlets_result.setNoDataTextColor(R.color.colorPrimaryDark);
            pc_moodlets_result.setNoDataTextTypeface(global.getCustomTypeface());

            pc_moodlets_result.getLegend().setEnabled(false);
        }

        public void expand_collapse(){
            if(ll_moodlets_content.getVisibility() == View.GONE){
                ll_moodlets_content.setVisibility(View.VISIBLE);
                ll_inner_separator.setVisibility(View.VISIBLE);
                tv_header_input_text.setVisibility(View.GONE);
            } else {
                ll_moodlets_content.setVisibility(View.GONE);
                ll_inner_separator.setVisibility(View.GONE);
                tv_header_input_text.setVisibility(View.VISIBLE);
            }
        }
    }
}
