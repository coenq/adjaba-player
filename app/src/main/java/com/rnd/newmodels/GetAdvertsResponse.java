
package com.rnd.newmodels;

import java.util.ArrayList;
import java.util.Date;

public class GetAdvertsResponse {
    public String userId;
    public String advertId;
    public boolean adPlaying;
    public int balance;
    public AdData adData;
    public Date dateCreated;
    public Object endDate;
    public ArrayList<String> excludeTags;
    public boolean isActive;
    public Object startDate;

    public GetAdvertsResponse(String userId, String advertId, boolean adPlaying, int balance, AdData adData, Date dateCreated, Object endDate, ArrayList<String> excludeTags, boolean isActive, Object startDate) {
        this.userId = userId;
        this.advertId = advertId;
        this.adPlaying = adPlaying;
        this.balance = balance;
        this.adData = adData;
        this.dateCreated = dateCreated;
        this.endDate = endDate;
        this.excludeTags = excludeTags;
        this.isActive = isActive;
        this.startDate = startDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getAdvertId() {
        return advertId;
    }

    public boolean isAdPlaying() {
        return adPlaying;
    }

    public int getBalance() {
        return balance;
    }

    public AdData getAdData() {
        return adData;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Object getEndDate() {
        return endDate;
    }

    public ArrayList<String> getExcludeTags() {
        return excludeTags;
    }

    public boolean isActive() {
        return isActive;
    }

    public Object getStartDate() {
        return startDate;
    }
}

