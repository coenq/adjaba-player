package com.adjaba.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImpressionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertImpression(ImpressionEntity impression);

    @Query("DELETE FROM impressions WHERE impressionId = :id")
    void deleteAdById(String id);

    @Query("SELECT * FROM impressions")
    List<ImpressionEntity> getAllImpressions();

    @Query("DELETE FROM impressions")
    void clearImpressions();

    // ── Proof-of-play (new in DB version 10) ──

    /** Rows not yet acknowledged by the backend — what ImpressionRetryWorker actually retries.
     *  (getAllImpressions() above now returns full play history, sent or not, for reporting.) */
    @Query("SELECT * FROM impressions WHERE sentToBackend = 0")
    List<ImpressionEntity> getPendingImpressions();

    @Query("UPDATE impressions SET sentToBackend = 1 WHERE impressionId = :id")
    void markSent(String id);

    /** Bounds table growth on a screen that runs for months — only trims rows already
     *  acknowledged by the backend, so nothing pending upload is ever lost. */
    @Query("DELETE FROM impressions WHERE sentToBackend = 1 AND playTimeStamp < :cutoffIso")
    void trimOldSentImpressions(String cutoffIso);

    @Query("SELECT COUNT(*) FROM impressions")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM impressions WHERE sentToBackend = 0")
    int getPendingCount();

    /** Per-ad play summary for ProofOfPlayActivity, most-played first. */
    @Query("SELECT advertId, format, COUNT(*) AS playCount, SUM(playSec) AS totalSeconds " +
           "FROM impressions GROUP BY advertId ORDER BY playCount DESC")
    List<AdPlaySummary> getSummaryByAd();

    /** Plain projection POJO — Room fills it by matching column/field names, no @Entity needed. */
    class AdPlaySummary {
        public String advertId;
        public String format;
        public int playCount;
        public int totalSeconds;
    }
}
