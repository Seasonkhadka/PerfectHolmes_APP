package com.example.perfectholmes.common.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetServiceAPI {
    @GET("create_token")
    Call<ResponseBody> create_token(@Query("user_id") String user_id, @Query("user_passwd") String user_passwd);

   }
