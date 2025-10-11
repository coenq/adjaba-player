package com.rnd.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "impressions", primaryKeys = {"screenViewId"})
public class ImpressionEntity {
    @NonNull

    public String screenId;
    @NonNull

    public String screenViewId;
    public boolean amountSettled;
    public String currency;
    public long dayHour;
    public double playSec;
    public int female20;
    public int female32;
    public int female40;
    public int female50;
    public int female50plus;
    public String format;
    public double impressionCost;
    public String locationType;
    public int male20;
    public int male32;
    public int male40;
    public int male50;
    public int male50plus;

    @TypeConverters(TagsConverter.class)
    public List<String> objectDetected;

    public String orientation;
    public String playTimeStamp;
    public String screenDevice;
    public String screenPlayer;
    public Long happy, sad,neutral;

    @TypeConverters(TagsConverter.class)
    public List<String> tags;

    @TypeConverters(TagsConverter.class)
    public List<String> textDetected;

    public int viewCount;


}

