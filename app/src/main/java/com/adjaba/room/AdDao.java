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

    @Query("SELECT * FROM ads WHERE advertId = :id LIMIT 1")
    AdEntity getAdById(String id);

    @Query("DELETE FROM ads")
    Completable deleteAllAds();

    // ── Smart Sync Queries ────────────────────────────────────────────────
    @Query("SELECT advertId FROM ads WHERE screenId = :screenId")
    List<String> getAdIdsByScreen(String screenId);

    @Query("DELETE FROM ads WHERE advertId = :advertId")
    void deleteAdById(String advertId);

    @Query("DELETE FROM ads WHERE screenId = :screenId AND advertId NOT IN (:keepIds)")
    void deleteAdsNotInList(String screenId, List<String> keepIds);
}
