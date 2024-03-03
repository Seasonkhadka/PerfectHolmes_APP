package com.example.perfectholmes_app.jsonclass;

import com.google.gson.annotations.SerializedName;

public class RegUser {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("password2")
    private String password2;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("phone_number")
    private String phoneNumber;

    public  RegUser(String username, String password, String password2, String nickname, String phoneNumber){
        this.username=username;
        this.password=password;
        this.password2=password2;
        this.nickname=nickname;
        this.phoneNumber=phoneNumber;

    }

}
