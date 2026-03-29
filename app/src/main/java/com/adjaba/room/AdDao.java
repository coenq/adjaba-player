package com.adjaba.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;


@Dao
public interface AdDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAd(AdEntity ad);
    @Query("SELECT * FROM ads")
    List<AdEntity> getAll();
    @Query("SELECT * FROM ads WHERE screenId = :id ORDER BY insertedAt ASC")
    List<AdEntity> getAllAds(String id);

    @Query("SELECT * FROM ads WHERE targetGender LIKE '%' || :gender || '%' LIMIT 1")
    AdEntity getAdByGender(String gender);

    @Query("DELETE FROM ads")
    Completable deleteAllAds();
}
