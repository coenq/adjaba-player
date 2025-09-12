package com.rnd.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "impressions")
public class ImpressionEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId; // ID داخلي للـ DB

     public String impressionId;
    public String advertId;
    public boolean amountSettled;
    public String contractId;
    public  String currency;
    public int dayHour;
    public int playSec;

    public int female20;
    public int female32;
    public int female40;
    public int female50;
    public int female50plus;

    public String format;
    public int impressionCost;
    public boolean isActiveContract;
    public String locationType;

    public int male20;
    public int male32;
    public int male40;
    public int male50;
    public int male50plus;

    public int maxBid;

    @TypeConverters(TagsConverter.class)
    public  List<String> objectDetected;
    public  String orientation;
    public  String playTimeStamp;
    public  String screenDevice;
    public  String screenPlayer;
    public String screenId;

    @TypeConverters(TagsConverter.class)
    public List<String> tags;

    @TypeConverters(TagsConverter.class)
    public  List<String> textDetected;

     public int viewCount;

}

