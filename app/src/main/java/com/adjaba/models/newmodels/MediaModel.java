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

    // ── Demographic targeting fields ──
    /** Comma-separated genders, e.g. "FEMALE,MALE" */
    String targetGender;
    /** Comma-separated age brackets, e.g. "0-20,20-32,32-42" */
    String targetAgeGroup;
    /** Comma-separated tags, e.g. "brand,promo" */
    String targetTags;
    /** Comma-separated emotions, e.g. "happy,neutral" */
    String targetEmotion;

    public MediaModel(String contractId, String currency, int maxBid, String type, String url,
                      int duration, String info, String displayText, String logo,
                      String targetHours, String advertId) {
        this.type = type;
        this.url = url;
        this.durationInMillis = duration;
        this.infoLink = info;
        this.displayText = displayText;
        this.logoUrl = logo;
        this.targetHours = targetHours;
        this.advertId = advertId;
        this.contractId = contractId;
        this.currency = currency;
        this.maxBid = maxBid;
    }

    /** Extended constructor that includes demographic targeting fields. */
    public MediaModel(String contractId, String currency, int maxBid, String type, String url,
                      int duration, String info, String displayText, String logo,
                      String targetHours, String advertId,
                      String targetGender, String targetAgeGroup, String targetTags, String targetEmotion) {
        this(contractId, currency, maxBid, type, url, duration, info, displayText, logo, targetHours, advertId);
        this.targetGender = targetGender;
        this.targetAgeGroup = targetAgeGroup;
        this.targetTags = targetTags;
        this.targetEmotion = targetEmotion;
    }

    public String getType()       { return type; }
    public String getInfoLink()   { return infoLink; }
    public String getLogoUrl()    { return logoUrl; }
    public String getContractId() { return contractId; }
    public String getCurrency()   { return currency; }
    public int    getMaxBid()     { return maxBid; }
    public String getLogo()       { return logoUrl; }
    public String getAdvertId()   { return advertId; }
    public String getTargetHours() { return targetHours; }
    public String getDisplayText() { return displayText; }
    public String getUrl()         { return url; }
    public String getInfo()        { return infoLink; }
    public int    getDurationInMillis() { return durationInMillis; }

    /** Returns comma-separated genders this ad targets, e.g. "FEMALE,MALE". May be null. */
    public String getTargetGender()    { return targetGender; }
    /** Returns comma-separated age brackets this ad targets, e.g. "20-32,32-42". May be null. */
    public String getTargetAgeGroup()  { return targetAgeGroup; }
    /** Returns comma-separated tags for this ad, e.g. "brand,promo". May be null. */
    public String getTargetTags()      { return targetTags; }
    /** Returns comma-separated emotions this ad targets, e.g. "happy,neutral". May be null. */
    public String getTargetEmotion()   { return targetEmotion; }
}
