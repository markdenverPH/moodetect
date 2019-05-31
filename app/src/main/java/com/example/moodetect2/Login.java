package com.example.moodetect2;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Login extends AppCompatActivity implements DialogInterface.OnDismissListener,
        DialogInterface.OnCancelListener, View.OnClickListener {

    Button btn_login, btn_register;
    TextInputLayout til_email, til_pass;
    LinearLayout base_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        til_email = findViewById(R.id.til_email);
        til_pass = findViewById(R.id.til_pass);
        base_layout = findViewById(R.id.base_layout);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistrationForm registrationForm = new RegistrationForm();
                registrationForm.show(getSupportFragmentManager(), "registration");
            }
        });
    }

    public void open_main_activity(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // ~~~~~ CONTINUE
        // cancelled the dialog; nothing to do
        Snackbar.make(base_layout, "Dialog cancelled", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // ~~~~~ CONTINUE
        // success register; registered user need to be verified
        Snackbar sb_dismiss_reg = Snackbar.make(base_layout, "We sent a verification link into your email.", Snackbar.LENGTH_INDEFINITE);
        sb_dismiss_reg.setAction("OK", this);
    }

    @Override
    public void onClick(View v) {
        Snackbar.make(base_layout, "clicked ok", Snackbar.LENGTH_LONG).show();
    }
}
