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

    // ── Proof-of-play (new in DB version 10) ──
    /** Set true once the backend has acknowledged this impression. Previously the row was
     *  deleted on a successful upload, which meant a successfully-synced screen had no local
     *  play history left to report on — only whatever was still stuck in the upload queue. Rows
     *  now persist (and age out via ImpressionRetryWorker's trim) so ProofOfPlayActivity can show
     *  real play history, not just a snapshot of what failed to upload. */
    public boolean sentToBackend = false;
}

