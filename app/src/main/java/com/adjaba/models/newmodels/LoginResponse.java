package com.adjaba.models.newmodels;

public class LoginResponse {
    public String message;
    public String userid;
    public String email;
    public String loginToken;

    public LoginResponse(String message, String userid, String email, String loginToken) {
        this.message = message;
        this.userid = userid;
        this.email = email;
        this.loginToken = loginToken;
    }

    public String getMessage() {
        return message;
    }

    public String getUserid() {
        return userid;
    }

    public String getEmail() {
        return email;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }
}
