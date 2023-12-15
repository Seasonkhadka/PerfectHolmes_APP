package com.example.perfectholmes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.perfectholmes.common.Constants;
import com.example.perfectholmes.common.retrofit.GetServiceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    EditText muserid,mPassword;
    Button mLoginBtn;
    TextView mRegisterNowBtn,mIdPwdForget;

    //FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        muserid=findViewById(R.id.emailtxt);
        mPassword=findViewById(R.id.passwordtxt);
        mLoginBtn=findViewById(R.id.loginbtn);
        mRegisterNowBtn=findViewById(R.id.registerNowBtn);
        mIdPwdForget=findViewById(R.id.idpwfindingbtn);











        mRegisterNowBtn.setOnClickListener(
                view -> startActivity(new Intent(LoginActivity.this,RegistrationActivity.class)));


        mLoginBtn.setOnClickListener(view -> {
            String user_id = muserid.getText().toString();
            String user_pd = mPassword.getText().toString();

            Retrofit retrofit= new Retrofit.Builder().baseUrl(Constants.LOGINREQUESTURL).addConverterFactory(GsonConverterFactory.create()).build();
            GetServiceAPI getServiceAPI=retrofit.create(GetServiceAPI.class);
            Call<ResponseBody> userDetailList=getServiceAPI.create_token(user_id,user_pd);
            userDetailList.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody result= response.body();
                    if(result!=null){
                        Log.i("Login",result.toString());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        });

    }
}