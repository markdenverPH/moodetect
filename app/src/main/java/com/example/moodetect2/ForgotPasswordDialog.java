package com.example.moodetect2;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog extends DialogFragment {

    View v;
    Button btn_cancel_fp, btn_confirm_fp;
    EditText et_email_fp;
    DialogForgotPassword onConfirm;
    Global global;

    public interface DialogForgotPassword{
        void onConfirm(String str);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_forgot_password_dialog, container, false);
        btn_cancel_fp = v.findViewById(R.id.btn_cancel_fp);
        btn_confirm_fp = v.findViewById(R.id.btn_confirm_fp);
        et_email_fp = v.findViewById(R.id.et_email_fp);
        global = new Global(getContext());

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().setWindowAnimations(R.style.ForgotPasswordDialogAnimation);

        // src: https://stackoverflow.com/questions/21307858/detect-back-button-but-dont-dismiss-dialogfragment
        // control back press
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if (keyCode ==  android.view.KeyEvent.KEYCODE_BACK && btn_cancel_fp.isEnabled()) {
                    return true;
                } else return false;
            }
        });

        btn_cancel_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        btn_confirm_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disable_buttons(true);
                String str_email = String.valueOf(et_email_fp.getText());
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if(!str_email.isEmpty()){
                    auth.sendPasswordResetEmail(str_email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // dismiss, trigger interface
                                        onConfirm.onConfirm("Email sent. Please check the link in the email.");
                                        if(getDialog() != null ) {
                                            getDialog().dismiss();
                                        }
                                    } else {
                                        error_occured(task);
                                    }
                                }
                            });
                } else {
                    global.error_occured("Please fill-up the email field.");
                    disable_buttons(false);
                }
            }
        });
    }

    public void setContext(DialogForgotPassword context){
        onConfirm = context;
    }

    private void disable_buttons(boolean value){
        if(value){
            btn_cancel_fp.setAlpha(0.4f);
            btn_cancel_fp.setEnabled(false);
            btn_confirm_fp.setAlpha(0.4f);
            btn_confirm_fp.setEnabled(false);
        } else {
            btn_cancel_fp.setAlpha(1);
            btn_cancel_fp.setEnabled(true);
            btn_confirm_fp.setAlpha(1);
            btn_confirm_fp.setEnabled(true);
        }
    }

    private void error_occured(Task task){
        global.error_occured(task);
        disable_buttons(false);
    }
}
