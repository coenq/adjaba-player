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

    // ── v1.1.0: Advanced content type fields ──
    /** Zone ID for multi-zone layouts (1-4), 0 = full screen */
    int zoneId = 0;
    /** For web content: HTML content or URL */
    String webContent;
    /** For live streams: Stream type (HLS, DASH, RTSP, HTTP) */
    String streamType;
    /** For social feeds: Platform name (TWITTER, INSTAGRAM, FACEBOOK) */
    String socialPlatform;
    /** For social feeds: Hashtag or username to filter */
    String socialHashtag;
    /** Is this a live stream? */
    boolean isLiveStream = false;
    /** Is this web content? */
    boolean isWebContent = false;
    /** Is this a social media feed? */
    boolean isSocialFeed = false;

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
    
    // ── v1.1.0: Getters and setters for advanced content types ──
    
    public int getZoneId() { return zoneId; }
    public void setZoneId(int zoneId) { this.zoneId = zoneId; }
    
    public String getWebContent() { return webContent; }
    public void setWebContent(String webContent) { 
        this.webContent = webContent;
        this.isWebContent = true;
    }
    
    public String getStreamType() { return streamType; }
    public void setStreamType(String streamType) { 
        this.streamType = streamType;
        this.isLiveStream = true;
    }
    
    public String getSocialPlatform() { return socialPlatform; }
    public void setSocialPlatform(String socialPlatform) { 
        this.socialPlatform = socialPlatform;
        this.isSocialFeed = true;
    }
    
    public String getSocialHashtag() { return socialHashtag; }
    public void setSocialHashtag(String socialHashtag) { this.socialHashtag = socialHashtag; }
    
    public boolean isLiveStream() { return isLiveStream; }
    public boolean isWebContent() { return isWebContent; }
    public boolean isSocialFeed() { return isSocialFeed; }
    
    /**
     * Factory method for creating live stream media
     */
    public static MediaModel createLiveStream(String streamUrl, String streamType, int zoneId) {
        MediaModel media = new MediaModel(null, "USD", 0, "LIVE_STREAM", streamUrl, 
                                          0, "", "", "", "", "stream_" + System.currentTimeMillis());
        media.setStreamType(streamType);
        media.setZoneId(zoneId);
        return media;
    }
    
    /**
     * Factory method for creating web content media
     */
    public static MediaModel createWebContent(String webUrl, int duration, int zoneId) {
        MediaModel media = new MediaModel(null, "USD", 0, "WEB_CONTENT", webUrl, 
                                          duration, "", "", "", "", "web_" + System.currentTimeMillis());
        media.setWebContent(webUrl);
        media.setZoneId(zoneId);
        return media;
    }
    
    /**
     * Factory method for creating social feed media
     */
    public static MediaModel createSocialFeed(String platform, String hashtag, int duration, int zoneId) {
        MediaModel media = new MediaModel(null, "USD", 0, "SOCIAL_FEED", "", 
                                          duration, "", "", "", "", "social_" + System.currentTimeMillis());
        media.setSocialPlatform(platform);
        media.setSocialHashtag(hashtag);
        media.setZoneId(zoneId);
        return media;
    }
}
