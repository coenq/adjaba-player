
package com.rnd.newmodels;

import java.util.ArrayList;

public class AdData {
    public int adBudget;
    public String currency;
    public int adDuration;
    public String adFormat;
    public int maxBid;
    public String orientation;
    public String resolution;
    public ArrayList<String> targetAgeGroup;
    public ArrayList<String> targetDevice;
    public ArrayList<String> targetEvent;
    public ArrayList<String> targetGender;
    public ArrayList<Integer> targetHours;
    public ArrayList<String> targetLocationType;
    public int targetMinSizeInch;
    public ArrayList<String> targetTags;
    public String textBottom;
    public String textLeft;
    public String textRight;
    public String textTop;
    public String videoUrl;

    public AdData(int adBudget, String currency, int adDuration, String adFormat, int maxBid, String orientation, String resolution, ArrayList<String> targetAgeGroup, ArrayList<String> targetDevice, ArrayList<String> targetEvent, ArrayList<String> targetGender, ArrayList<Integer> targetHours, ArrayList<String> targetLocationType, int targetMinSizeInch, ArrayList<String> targetTags, String textBottom, String textLeft, String textRight, String textTop, String videoUrl) {
        this.adBudget = adBudget;
        this.currency = currency;
        this.adDuration = adDuration;
        this.adFormat = adFormat;
        this.maxBid = maxBid;
        this.orientation = orientation;
        this.resolution = resolution;
        this.targetAgeGroup = targetAgeGroup;
        this.targetDevice = targetDevice;
        this.targetEvent = targetEvent;
        this.targetGender = targetGender;
        this.targetHours = targetHours;
        this.targetLocationType = targetLocationType;
        this.targetMinSizeInch = targetMinSizeInch;
        this.targetTags = targetTags;
        this.textBottom = textBottom;
        this.textLeft = textLeft;
        this.textRight = textRight;
        this.textTop = textTop;
        this.videoUrl = videoUrl;
    }

    public int getAdBudget() {
        return adBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAdDuration() {
        return adDuration;
    }

    public String getAdFormat() {
        return adFormat;
    }

    public int getMaxBid() {
        return maxBid;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getResolution() {
        return resolution;
    }

    public ArrayList<String> getTargetAgeGroup() {
        return targetAgeGroup;
    }

    public ArrayList<String> getTargetDevice() {
        return targetDevice;
    }

    public ArrayList<String> getTargetEvent() {
        return targetEvent;
    }

    public ArrayList<String> getTargetGender() {
        return targetGender;
    }

    public ArrayList<Integer> getTargetHours() {
        return targetHours;
    }

    public ArrayList<String> getTargetLocationType() {
        return targetLocationType;
    }

    public int getTargetMinSizeInch() {
        return targetMinSizeInch;
    }

    public ArrayList<String> getTargetTags() {
        return targetTags;
    }

    public String getTextBottom() {
        return textBottom;
    }

    public String getTextLeft() {
        return textLeft;
    }

    public String getTextRight() {
        return textRight;
    }

    public String getTextTop() {
        return textTop;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
