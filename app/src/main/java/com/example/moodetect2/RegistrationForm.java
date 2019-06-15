package com.example.moodetect2;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationForm extends DialogFragment {
    View v;
    Button btn_cancel, btn_register;
    TextView tv_errors;
    EditText et_firstname, et_lastname, et_email, et_pass, et_confirm_pass;
    FirebaseAuth firebaseAuth;
    LinearLayout reg_base_layout;
    boolean reg_status;
    TextInputLayout til_pass, til_confirm_pass;
    Global global;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_registration_form, container, false);
        btn_cancel = v.findViewById(R.id.btn_cancel);
        btn_register = v.findViewById(R.id.btn_register);
        tv_errors = v.findViewById(R.id.tv_errors);
        et_firstname = v.findViewById(R.id.et_firstname);
        et_lastname = v.findViewById(R.id.et_lastname);
        et_email = v.findViewById(R.id.et_email);
        et_pass = v.findViewById(R.id.et_pass);
        et_confirm_pass = v.findViewById(R.id.et_confirm_pass);
        til_pass = v.findViewById(R.id.til_pass);
        til_confirm_pass = v.findViewById(R.id.til_confirm_pass);
        reg_base_layout = v.findViewById(R.id.reg_base_layout);
        global = new Global(getContext());
        reg_status = false;

        til_pass.setTypeface(global.getCustomTypeface());
        til_confirm_pass.setTypeface(global.getCustomTypeface());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);

        // src: https://stackoverflow.com/questions/21307858/detect-back-button-but-dont-dismiss-dialogfragment
        // control back press
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if (keyCode ==  android.view.KeyEvent.KEYCODE_BACK && btn_cancel.isEnabled()) {
//                    dismiss();
                    return true;
                } else return false;
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_default_values();
                getDialog().dismiss();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disable_buttons(true);

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
                    disable_buttons(false);
                } else {
                    tv_errors.setVisibility(View.GONE);
                    register_user(str_email, str_pass, str_firstname, str_lastname);
                }
            }
        });
    }

    private void register_user(final String str_email, final String str_pass, final String str_firstname, final String str_lastname){
        firebaseAuth = FirebaseAuth.getInstance();
        // auto signed in
        firebaseAuth.createUserWithEmailAndPassword(str_email, str_pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            insert_data();
                        } else {
                            Log.d("check_on", "error in create_user");
                            error_occured(task);
                        }
                    }

                    private void insert_data() {
                        // put value in firestore
                        String uid = firebaseAuth.getUid();
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        CollectionReference collection_users = firebaseFirestore.collection("users");
                        Map<String, Object> data = new HashMap<>();
                        data.put("first_name", str_firstname);
                        data.put("last_name", str_lastname);
                        data.put("email", str_email);
                        data.put("uid", uid);
                        data.put("first_login", false);
                        collection_users.document(uid).set(data).addOnCompleteListener(new OnCompleteListener<Void>(){

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    send_verification();
                                } else {
                                    Log.d("check_on", "error in insertion of data");
                                    error_occured(task);
                                }
                            }
                        });
                    }

                    private void send_verification(){
                        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        firebaseUser.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        // Re-enable button
                                        if (task.isSuccessful()) {
                                            set_default_values();
                                            reg_status = true;
                                            firebaseAuth.signOut();
                                            if(getDialog() != null ) {
                                                getDialog().dismiss();
                                            }
                                        } else {
                                            Log.d("check_on", "error in send email verification");
                                            error_occured(task);
                                        }
                                    }
                                });
                    }
                });
    }

    private void error_occured(Task task){
        Global global = new Global(getContext());
        global.error_occured(task);
        reg_status = false;
        disable_buttons(false);
    }

    private void disable_buttons(boolean value){
        if(value){
            btn_cancel.setAlpha(0.4f);
            btn_cancel.setEnabled(false);
            btn_register.setAlpha(0.4f);
            btn_register.setEnabled(false);
        } else {
            btn_cancel.setAlpha(1);
            btn_cancel.setEnabled(true);
            btn_register.setAlpha(1);
            btn_register.setEnabled(true);
        }
    }

    public boolean getReg_status(){
        return reg_status;
    }

    // src: https://stackoverflow.com/questions/23786033/dialogfragment-and-ondismiss
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private void set_default_values(){
        et_firstname.setText("");
        et_lastname.setText("");
        et_email.setText("");
        et_pass.setText("");
        et_confirm_pass.setText("");
    }

}
