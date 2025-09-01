package com.adjaba.models.newmodels;

public class LoginRequest {
    public String userId;
    public String password;

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
