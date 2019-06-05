package com.example.moodetect2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity implements DialogInterface.OnDismissListener {

    Button btn_login, btn_register;
    TextInputLayout til_email, til_pass;
    LinearLayout base_layout;
    RegistrationForm registrationForm;
    FirebaseAuth firebaseAuth;
    EditText et_email, et_pass;
    Global global;

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
        et_email = findViewById(R.id.et_email);
        et_pass = findViewById(R.id.et_pass);
        firebaseAuth = FirebaseAuth.getInstance();
        global = new Global(getApplicationContext());

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // double click error will occur
                if(!registrationForm.isVisible()){
                    registrationForm.show(getSupportFragmentManager(), "registration");
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String email = String.valueOf(et_email.getText());
//                String pass = String.valueOf(et_pass.getText());
                String email = "babaranmark@yahoo.com";
                String pass = "mark1234";

                firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // save some info to shared preference
                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                    if(firebaseUser.isEmailVerified()){
                                        // fetch and save to preference
                                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                        DocumentReference docRef = firebaseFirestore
                                                .collection("users")
                                                .document(firebaseUser.getUid());
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        UserProfile userProfile = document.toObject(UserProfile.class);
                                                        userProfile.save_basic_info(getApplicationContext());

                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Snackbar.make(base_layout, "An error occured.", Snackbar.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    Log.d("check_on", "document does not exist");
                                                    global.error_occured(task);
                                                }
                                            }
                                        });
                                    } else {
                                        firebaseAuth.signOut();
                                        Snackbar.make(base_layout, "Please verify your email first.", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    global.error_occured(task);
                                    Log.d("check_on", "failed sign in");
                                }
                            }
                        });
            }
        });
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
