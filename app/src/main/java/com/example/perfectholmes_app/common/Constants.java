package com.example.perfectholmes_app.common;

public interface Constants {
    String URLPROTOCOL = "http://";
    String HTTPServer = "43.202.188.79";
   // String PORT = "443";
    String MAIN_URL = URLPROTOCOL + HTTPServer;

    //login request
    String LOGINREQUESTURL = MAIN_URL + "/auth/";

    //registration
    String REGISTRATIONURL = MAIN_URL + "/auth/signup/";


}
