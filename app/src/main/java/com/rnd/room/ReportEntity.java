package com.rnd.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reports")
public class ReportEntity {
    public int female20;
    public int female32;
    public int female40;
    public int female50;
    public int female50plus;
    public int male20;
    public int male32;
    public int male40;
    public int male50;
    public int male50plus;
    public long happy;
    public long neutral;
    public long sad;
    public long child;
    public long adult;
    public long middle;
    public long senior;
    public int hour;
    public int day;
    @PrimaryKey(autoGenerate = true)
    int id;
}
