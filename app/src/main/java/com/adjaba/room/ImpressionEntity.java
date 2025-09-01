package com.adjaba.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "impressions")
public class ImpressionEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId; // ID داخلي للـ DB

    public String impressionId;
    public String advertId;
    public boolean amountSettled;
    public String contractId;
    public String currency;
    public int dayHour;
    public int playSec;
    public String format;
    public String locationType;
    public double maxBid;
    public String orientation;
    public String playTimeStamp;
    public String screenDevice;
    public String screenPlayer;
    public String screenId;
    @TypeConverters(TagsConverter.class)
    public List<String> tags;;
}

