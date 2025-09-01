package com.adjaba.models.newmodels;

public class VideoImageModel {
    public String status;
    public String url;

    public VideoImageModel(String status, String url) {
        this.status = status;
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }
}
