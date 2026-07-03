package com.adjaba.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One cached image downloaded from a Cloud Slideshow Google Drive folder.
 * Keyed by the Drive file ID so re-syncing can skip unchanged files
 * (see SlideshowManager) and drop ones removed from the folder.
 */
@Entity(tableName = "slideshow_images")
public class SlideshowImageEntity {
    @PrimaryKey
    @NonNull
    public String fileId;

    public String folderId;
    public String localPath;
    /** Drive's modifiedTime (ISO-8601) or a hash, used to detect changed files without a re-download. */
    public String modifiedTime;
    public long insertedAt;

    public SlideshowImageEntity(@NonNull String fileId, String folderId, String localPath,
                                 String modifiedTime, long insertedAt) {
        this.fileId = fileId;
        this.folderId = folderId;
        this.localPath = localPath;
        this.modifiedTime = modifiedTime;
        this.insertedAt = insertedAt;
    }
}
