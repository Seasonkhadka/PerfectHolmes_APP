package com.example.perfectholmes_app.common.retrofit;

import com.example.perfectholmes_app.jsonclass.RegUser;
import com.example.perfectholmes_app.jsonclass.User;
import com.example.perfectholmes_app.jsonclass.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GetServiceAPI {
    @POST("login")
    Call<ResponseBody> create_token(@Body User user);

    @POST("registerUser.json")
    Call<ResponseBody> registerUser(@Body RegUser regUser);
}


