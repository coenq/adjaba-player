package com.adjaba.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "info")
public class InfoEntity {
    @PrimaryKey(autoGenerate = true)
    public int infoId;
    public String info;

    public InfoEntity(String info) {
        this.info = info;
    }
}
