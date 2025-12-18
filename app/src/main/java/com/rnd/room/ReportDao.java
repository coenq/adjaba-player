package com.rnd.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReport(ReportEntity reportEntity);

    @Query("SELECT * FROM reports where day = :day")
    List<ReportEntity> getAllReports(int day);

    @Query("SELECT * FROM reports where hour = :hour and day=:day")
    List<ReportEntity> getAllReportsByHour(int hour,int day);
    @Query("DELETE FROM reports")
    void clearReports();
}
