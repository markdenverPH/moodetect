package com.example.moodetect2;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Login extends AppCompatActivity implements DialogInterface.OnDismissListener {

    Button btn_login, btn_register;
    TextInputLayout til_email, til_pass;
    LinearLayout base_layout;
    RegistrationForm registrationForm;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        til_email = findViewById(R.id.til_email);
        til_pass = findViewById(R.id.til_pass);
        base_layout = findViewById(R.id.base_layout);
        registrationForm = new RegistrationForm();
        til_pass.setTypeface(ResourcesCompat.getFont(this, R.font.varela_round));
        til_email.setTypeface(ResourcesCompat.getFont(this, R.font.varela_round));

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // double click error will occur
                if(!registrationForm.isVisible()){
                    registrationForm.show(getSupportFragmentManager(), "registration");
                }
            }
        });
    }

    public void open_main_activity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // success register; registered user need to be verified
        if(registrationForm.getReg_status()){
            Snackbar sb_dismiss_reg = Snackbar.make(base_layout, "We sent a verification link into your email.", 4000);
            sb_dismiss_reg.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("check_on", "CLICKED OK SNACKBAR");
                }
            });
            sb_dismiss_reg.show();
        }
    }
}
