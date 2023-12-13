package com.example.perfectholmes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    EditText mEmail,mPassword;
    Button mLoginBtn;
    TextView mRegisterNowBtn,mIdPwdForget;

    //FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail=findViewById(R.id.emailtxt);
        mPassword=findViewById(R.id.passwordtxt);
        mLoginBtn=findViewById(R.id.loginbtn);
        mRegisterNowBtn=findViewById(R.id.registerNowBtn);
        mIdPwdForget=findViewById(R.id.idpwfindingbtn);


        mRegisterNowBtn.setOnClickListener(
                view -> startActivity(new Intent(LoginActivity.this,RegistrationActivity.class)));



    }
}