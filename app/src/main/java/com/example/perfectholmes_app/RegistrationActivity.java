package com.example.perfectholmes_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.perfectholmes_app.common.Constants;
import com.example.perfectholmes_app.common.retrofit.GetServiceAPI;
import com.example.perfectholmes_app.jsonclass.RegUser;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegistrationActivity extends AppCompatActivity {
    EditText RFullName,Rnickname,RPassword,Rconpassword,Rphoneno;
    Button RRegisterBtn;
    TextView RLoginNowBtn;
     String fullNameTxt,nicknameTxt,passwordTxt,conPasswordTxt,phonenoTxt;
    RegUser reguser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        RFullName=findViewById(R.id.nameTxt);
        Rphoneno=findViewById(R.id.phonenoTxt);
        Rnickname=findViewById(R.id.nicknametxt);
        RPassword=findViewById(R.id.RegPwdTxt);
        Rconpassword=findViewById(R.id.confPwdTxt);
        RRegisterBtn=findViewById(R.id.regbtn);
        RLoginNowBtn=findViewById(R.id.loginNowBtn);

        RLoginNowBtn.setOnClickListener(view -> startActivity(new Intent(RegistrationActivity.this,LoginActivity.class)));

        RRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullNameTxt=RFullName.getText().toString();
                phonenoTxt=Rphoneno.getText().toString();
                nicknameTxt=Rnickname.getText().toString();
                passwordTxt=RPassword.getText().toString();
                conPasswordTxt=Rconpassword.getText().toString();
                 reguser= new RegUser(fullNameTxt,passwordTxt,conPasswordTxt,nicknameTxt,phonenoTxt);

                if (TextUtils.isEmpty(fullNameTxt)){
                    RFullName.setError("username is required");
                }
                if(TextUtils.isEmpty(nicknameTxt)){
                    Rnickname.setError("email is required");
                    return;
                }
                if(TextUtils.isEmpty(phonenoTxt)){
                    Rphoneno.setError("email is required");
                    return;
                }
                if (TextUtils.isEmpty(passwordTxt)){
                    RPassword.setError("password is required");
                    return;
                }
                if (passwordTxt.length()<8){
                    RPassword.setError("Password Must be greater than 8 character");
                    return;
                }
                if(!passwordTxt.equals(conPasswordTxt)) {
                    Toast.makeText(RegistrationActivity.this, "암호가 서로 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }
                registerUser();

             //

            }
        });


    }


    private void registerUser() {
        HashMap<String, Object> params = new HashMap<>();
       /* params.put("username",fullNameTxt);
        params.put("password",passwordTxt);
        params.put("nickname",nicknameTxt);
        params.put("phone_number",phonenoTxt);
*/
        Retrofit retrofit=new Retrofit.Builder().baseUrl(Constants.REGISTRATIONURL).build();
        GetServiceAPI getServiceAPI=retrofit.create(GetServiceAPI.class);

        Call<ResponseBody> userIdCheck=getServiceAPI.registerUser(reguser);
        userIdCheck.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Intent intent=new Intent(RegistrationActivity.this,LoginActivity.class);
                    startActivity(intent);

                    // Handle successful registration response
                } else {
                    // Handle unsuccessful registration response
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
}