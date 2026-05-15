package com.adjaba.models.newmodels;

import com.google.gson.annotations.SerializedName;

public class TvPollAuthResponse {
    public String status;
    public String token;
    public TvUser user;

    public static class TvUser {
        @SerializedName("userId")
        public String userId;
        public String email;
        public String userType;
        public boolean isPartner;
        public boolean isAdmin;
        public String businessName;
    }
}
