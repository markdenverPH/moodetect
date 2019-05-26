package com.example.moodetect2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    Button btn_login, btn_register;
    TextInputLayout til_email, til_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        til_email = findViewById(R.id.til_email);
        til_pass = findViewById(R.id.til_pass);

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


    public static class RegistrationForm extends DialogFragment {
        View v;
        Button btn_cancel, btn_register;
        TextView tv_errors;
        EditText et_firstname, et_lastname, et_email, et_pass, et_confirm_pass;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.activity_registration_form, container, false);

            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//                getDialog().setCanceledOnTouchOutside(false);
            }

            return v;
        }

        @Override
        public void onActivityCreated(Bundle arg0) {
            super.onActivityCreated(arg0);
//            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            btn_cancel = v.findViewById(R.id.btn_cancel);
            btn_register = v.findViewById(R.id.btn_register);
            tv_errors = v.findViewById(R.id.tv_errors);
            et_firstname = v.findViewById(R.id.et_firstname);
            et_lastname = v.findViewById(R.id.et_lastname);
            et_email = v.findViewById(R.id.et_email);
            et_pass = v.findViewById(R.id.et_pass);
            et_confirm_pass = v.findViewById(R.id.et_confirm_pass);

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });

            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str_firstname, str_lastname, str_email, str_pass, str_confirm_pass, error_message = "";

                    str_firstname = String.valueOf(et_firstname.getText());
                    str_lastname = String.valueOf(et_lastname.getText());
                    str_email = String.valueOf(et_email.getText());
                    str_pass = String.valueOf(et_pass.getText());
                    str_confirm_pass = String.valueOf(et_confirm_pass.getText());

                    if(str_firstname.equalsIgnoreCase("") || str_lastname.length() == 0 ||
                            str_email.length() == 0 || str_pass.length() == 0 ||
                            str_confirm_pass.length() == 0 ){
                        error_message = "* All fields are required to be fill-up.";
                    }

                    if(str_firstname.length() > 30){
                        error_message = "* First Name is limited to 30 characters.";
                    }

                    if(str_lastname.length() > 30) {
                        if(error_message.length() > 0) {
                            error_message += "\n* Last Name is limited to 30 characters.";
                        } else {
                            error_message = "* Last Name is limited to 30 characters.";
                        }
                    }

                    if(str_pass.compareTo(str_confirm_pass) != 0) {
                        if(error_message.length() > 0) {
                            error_message += "\n* Password do not match";
                        } else {
                            error_message = "* Password do not match";
                        }
                    }

                    if(error_message.length() > 0){
                        tv_errors.setText(error_message);
                        tv_errors.setVisibility(View.VISIBLE);
                    } else {
                        tv_errors.setVisibility(View.GONE);

                        // register with firebase auth
                    }
                }
            });
        }
    }
}
