package com.adjaba.models.newmodels;

import java.util.ArrayList;
import java.util.Date;

public class AdContractData {
    public String advertId;
    public Date dateCreated;
    public String endDate;
    public String format;
    public String startDate;
    
    // ── Demographic & IOT Targeting Fields ──
    public ArrayList<String> targetAgeGroup;      // e.g. ["0-20", "20-32", "32-42", "42-50", "50+"]
    public ArrayList<String> targetDevice;
    public ArrayList<String> targetEvent;
    public ArrayList<String> targetGender;        // e.g. ["MALE", "FEMALE"]
    public ArrayList<Integer> targetHours;        // e.g. [9, 10, 11, 14, 15, 16]
    public Object targetLocationType;
    public ArrayList<String> targetTags;          // e.g. ["brand", "promo", "sports"]
    public ArrayList<String> targetEmotion;       // e.g. ["happy", "neutral"] - NEW for IOT
    
    // ── Content & Display Fields ──
    public String targeturl;
    public String textBottom;
    public String textLeft;
    public String textRight;
    public String textTop;
    public String videoUrl;

    // ── Content Type Fields (new for streaming, web, social media) ──
    public String streamType;         // For live_stream: "HLS", "DASH", "RTSP", "HTTP"
    public String socialPlatform;     // For social_feed: "TWITTER", "INSTAGRAM", "FACEBOOK"
    public String socialHashtag;      // For social_feed: hashtag or @username to display
}
