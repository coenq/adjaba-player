package com.adjaba.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "ads")
public class AdEntity {
    @PrimaryKey
    @NonNull
    public String advertId;

    public String format;
    public String localPath;
    /** Display text shown as the ad's caption overlay. */
    public String textTop;
    /** The ad's target URL — rendered as the on-screen QR code (empty = no QR). */
    public String textBottom;
    public String textLeft;
    public String textRight;
    public int duration;
    public String orientation;
    public String screenId;
    public String targetHours;
    public String contractId;
    public String currency;
    public int maxBid;
    public int insertedAt;

    // ── Demographic targeting fields (new in DB version 6) ──
    /** Comma-separated genders this ad targets, e.g. "FEMALE,MALE" */
    public String targetGender;
    /** Comma-separated age brackets this ad targets, e.g. "0-20,20-32,32-42" */
    public String targetAgeGroup;
    /** Comma-separated tags for this ad, e.g. "brand,promo" */
    public String targetTags;
    /** Comma-separated emotions this ad targets, e.g. "happy,neutral" - NEW in DB v7 */
    public String targetEmotion;
    
    // ── Content type fields (new in DB version 8) ──
    /** Stream type for live streams: "HLS", "DASH", "RTSP", "HTTP" */
    public String streamType;
    /** Social platform for social feeds: "TWITTER", "INSTAGRAM", "FACEBOOK" */
    public String socialPlatform;
    /** Social hashtag/username to display feed for, e.g. "#nike" or "@nike" */
    public String socialHashtag;

    // ── Full constructor with all fields (new in DB v8) ──
    public AdEntity(@NonNull String advertId, String format, String localPath, String textTop,
                    String textBottom, String textLeft, String textRight, int duration,
                    String orientation, String screenId, String contractId, String targetHours,
                    int insertedAt, String currency, int maxBid,
                    String targetGender, String targetAgeGroup, String targetTags, String targetEmotion,
                    String streamType, String socialPlatform, String socialHashtag) {
        this.advertId = advertId;
        this.format = format;
        this.localPath = localPath;
        this.textTop = textTop;
        this.textBottom = textBottom;
        this.textLeft = textLeft;
        this.textRight = textRight;
        this.duration = duration;
        this.orientation = orientation;
        this.screenId = screenId;
        this.contractId = contractId;
        this.targetHours = targetHours;
        this.insertedAt = insertedAt;
        this.currency = currency;
        this.maxBid = maxBid;
        this.targetGender = targetGender;
        this.targetAgeGroup = targetAgeGroup;
        this.targetTags = targetTags;
        this.targetEmotion = targetEmotion;
        this.streamType = streamType;
        this.socialPlatform = socialPlatform;
        this.socialHashtag = socialHashtag;
    }
    
    // ── Backward compatibility constructor (DB v7 and earlier) ──
    @androidx.room.Ignore
    public AdEntity(@NonNull String advertId, String format, String localPath, String textTop,
                    String textBottom, String textLeft, String textRight, int duration,
                    String orientation, String screenId, String contractId, String targetHours,
                    int insertedAt, String currency, int maxBid,
                    String targetGender, String targetAgeGroup, String targetTags, String targetEmotion) {
        this(advertId, format, localPath, textTop, textBottom, textLeft, textRight, duration,
             orientation, screenId, contractId, targetHours, insertedAt, currency, maxBid,
             targetGender, targetAgeGroup, targetTags, targetEmotion, null, null, null);
    }
}
