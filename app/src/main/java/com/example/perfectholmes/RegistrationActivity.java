package com.example.perfectholmes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RegistrationActivity extends AppCompatActivity {
    EditText RFullName,REmail,RPassword,Rconpassword;
    Button RRegisterBtn;
    TextView RLoginNowBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        RFullName=findViewById(R.id.nameTxt);
        REmail=findViewById(R.id.regemailTxt);
        RPassword=findViewById(R.id.RegPwdTxt);
        Rconpassword=findViewById(R.id.confPwdTxt);
        RRegisterBtn=findViewById(R.id.regbtn);
        RLoginNowBtn=findViewById(R.id.loginNowBtn);
        RLoginNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));

            }
        });


    }
}