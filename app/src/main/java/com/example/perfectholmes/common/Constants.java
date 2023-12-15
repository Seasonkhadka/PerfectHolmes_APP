package com.example.perfectholmes.common;

public interface Constants {
    String URLPROTOCOL = "https://";
    String HTTPServer = "43.202.188.79";
   // String PORT = "443";
    String MAIN_URL = URLPROTOCOL + HTTPServer;

    //login request
    String LOGINREQUESTURL = MAIN_URL + "/auth/login";

    //registration
    String REGISTRATIONURL = MAIN_URL + "/auth/signup";


}
