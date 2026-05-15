package com.adjaba.models.newmodels;

import com.google.gson.annotations.SerializedName;

public class TvStartAuthResponse {
    @SerializedName("device_code")
    public String deviceCode;

    @SerializedName("user_code")
    public String userCode;

    @SerializedName("qr_url")
    public String qrUrl;

    @SerializedName("expires_in")
    public int expiresIn;

    @SerializedName("poll_interval")
    public int pollInterval;
}
