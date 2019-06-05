package com.example.moodetect2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserProfile {
    private String email, first_name, last_name, uid;
    boolean first_login;

    public UserProfile(){}

    public UserProfile(String email, boolean first_login, String first_name, String last_name, String uid) {
        this.email = email;
        this.first_login = first_login;
        this.first_name = first_name;
        this.last_name = last_name;
        this.uid = uid;
    }

    public void save_basic_info(Context context){
        Log.d("check_on", "putting into sharedprefs: " + this.email);
        SharedPreferences sharedPref = context.getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("bi_email", this.email);
        editor.putBoolean("bi_first_login", this.first_login);
        editor.putString("bi_first_name", this.first_name);
        editor.putString("bi_last_name", this.last_name);
        editor.putString("bi_uid", this.uid);
        editor.commit();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getFirst_login() {
        return first_login;
    }

    public void setFirst_login(boolean first_login) {
        this.first_login = first_login;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
