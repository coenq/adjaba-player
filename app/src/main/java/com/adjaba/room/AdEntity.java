package com.adjaba.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "ads")
@TypeConverters(TagsConverter.class)
public class AdEntity {
    @PrimaryKey
    @NonNull
    public String advertId;

    public String format;
    public String localPath;
    public String textTop;
    public String textBottom;
    public String textLeft;
    public String textRight;
    public int duration;
    public String orientation;
    public String screenId;
    public String targetHours;
    public String contractId;
    public  String currency;
    public int maxBid;
    public int insertedAt;
    //@TypeConverters(TagsConverter.class)
    public List<String> targetAge;;
    public List<String> targetGender;


    public AdEntity(@NonNull String advertId,List<String>targetGender,List<String>targetAge, String format, String localPath, String textTop,
                    String textBottom, String textLeft, String textRight, int duration,
                    String orientation, String screenId, String contractId,String targetHours, int insertedAt,String currency,int maxBid) {
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
        this.targetHours=targetHours;
        this.insertedAt=insertedAt;
        this.currency=currency;
        this.maxBid=maxBid;
        this.targetAge=targetAge;
        this.targetGender=targetGender;
    }


}
