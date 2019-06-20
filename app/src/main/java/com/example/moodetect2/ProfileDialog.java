package com.example.moodetect2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ProfileDialog extends DialogFragment {

    View v;
    TextView tv_pd_fullname, tv_pd_email;
    SharedPreferences sharedPref;
    Button btn_pd_close;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_profile_dialog, container, false);
        tv_pd_fullname = v.findViewById(R.id.tv_pd_fullname);
        tv_pd_email = v.findViewById(R.id.tv_pd_email);
        sharedPref = getContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        btn_pd_close = v.findViewById(R.id.btn_pd_close);

        String str_fullname = sharedPref.getString("bi_first_name", "") + "\n" +
                sharedPref.getString("bi_last_name", "");
        String str_email = sharedPref.getString("bi_email", "");
        tv_pd_fullname.setText(str_fullname);
        tv_pd_email.setText(str_email);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btn_pd_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}
