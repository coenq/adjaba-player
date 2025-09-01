package com.adjaba.models.newmodels;

import com.adjaba.others.TargetHours;

import java.util.List;

public class MediaModel {
    String type;
    String url;
    private int durationInMillis;
    String infoLink;
    String displayText;
    String logoUrl;
    String targetHours;
    String advertId;
    String contractId;
    String currency;
    int maxBid;

    public MediaModel(String contractId,String currency,int maxBid,String type, String url, int duration, String info, String displayText, String logo, String targetHours, String advertId) {
        this.type = type;
        this.url = url;
        this.durationInMillis = duration;
        this.infoLink = info;
        this.displayText = displayText;
        this.logoUrl = logo;
        this.targetHours = targetHours;
        this.advertId = advertId;
        this.contractId=contractId;
        this.currency=currency;
        this.maxBid=maxBid;
    }

    public String getType() {
        return type;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getContractId() {
        return contractId;
    }

    public String getCurrency() {
        return currency;
    }

    public int getMaxBid() {
        return maxBid;
    }

    public String getLogo() {
        return logoUrl;
    }

    public String getAdvertId() {
        return advertId;
    }

    public String getTargetHours() {
        return targetHours;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getUrl() {
        return url;
    }

    public String getInfo() {
        return infoLink;
    }

    public int getDurationInMillis() {
        return durationInMillis;
    }
}
