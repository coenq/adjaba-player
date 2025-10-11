package com.rnd.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImpressionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertImpression(ImpressionEntity impression);
    @Query("DELETE FROM impressions WHERE screenViewId = :id")
    void deleteAdById(String id);
    @Query("SELECT * FROM impressions")
    List<ImpressionEntity> getAllImpressions();

    @Query("DELETE FROM impressions")
    void clearImpressions();
}
