package com.example.moodetect2;

import java.util.List;

public class MoodletsModel {

    private String date_time, input_text, sugg_emotion;
    private int sugg_index;
    private List<String> emotion_titles, emotion_vals;
    private long int_time;

    public MoodletsModel() {}

    public MoodletsModel(String date_time, List<String> emotion_titles, List<String> emotion_vals, String input_text,
                         String sugg_emotion, int sugg_index, long int_time) {
        this.date_time = date_time;
        this.emotion_titles = emotion_titles;
        this.emotion_vals = emotion_vals;
        this.input_text = input_text;
        this.sugg_emotion = sugg_emotion;
        this.sugg_index = sugg_index;
        this.int_time = int_time;
    }

    public long getInt_time() {
        return int_time;
    }

    public void setInt_time(long int_time) {
        this.int_time = int_time;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getInput_text() {
        return input_text;
    }

    public void setInput_text(String input_text) {
        this.input_text = input_text;
    }

    public String getSugg_emotion() {
        return sugg_emotion;
    }

    public void setSugg_emotion(String sugg_emotion) {
        this.sugg_emotion = sugg_emotion;
    }

    public int getSugg_index() {
        return sugg_index;
    }

    public void setSugg_index(int sugg_index) {
        this.sugg_index = sugg_index;
    }

    public List<String> getEmotion_titles() {
        return emotion_titles;
    }

    public void setEmotion_titles(List<String> emotion_titles) {
        this.emotion_titles = emotion_titles;
    }

    public List<String> getEmotion_vals() {
        return emotion_vals;
    }

    public void setEmotion_vals(List<String> emotion_vals) {
        this.emotion_vals = emotion_vals;
    }
}
