package com.adjaba.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SlideshowImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(SlideshowImageEntity image);

    @Query("SELECT * FROM slideshow_images WHERE folderId = :folderId ORDER BY insertedAt ASC")
    List<SlideshowImageEntity> getImagesForFolder(String folderId);

    @Query("SELECT * FROM slideshow_images ORDER BY insertedAt ASC")
    List<SlideshowImageEntity> getAllImages();

    @Query("SELECT fileId FROM slideshow_images WHERE folderId = :folderId")
    List<String> getFileIdsForFolder(String folderId);

    @Query("SELECT * FROM slideshow_images WHERE fileId = :fileId LIMIT 1")
    SlideshowImageEntity getByFileId(String fileId);

    @Query("DELETE FROM slideshow_images WHERE fileId = :fileId")
    void deleteByFileId(String fileId);

    @Query("DELETE FROM slideshow_images WHERE folderId != :keepFolderId")
    void deleteAllExceptFolder(String keepFolderId);

    @Query("DELETE FROM slideshow_images")
    void deleteAll();
}
