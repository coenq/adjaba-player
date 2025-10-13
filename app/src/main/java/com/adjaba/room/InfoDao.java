package com.adjaba.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface InfoDao {
    @Insert
    void insertInfo(InfoEntity infoEntity);
    @Query("SELECT * FROM info")
    List<InfoEntity> getAllInfo();

    @Query("DELETE FROM info")
    void clearInfo();
}
