package com.adjaba.utilities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Singleton manager for playlist sync events.
 * Replaces deprecated LocalBroadcastManager with LiveData pattern.
 *
 * Usage:
 * - Workers call notifyPlaylistUpdated() to broadcast updates
 * - Activities observe getPlaylistUpdateLiveData() to receive updates
 */
public class PlaylistSyncManager {

    private static PlaylistSyncManager instance;
    private final MutableLiveData<PlaylistUpdate> playlistUpdateLiveData;

    private PlaylistSyncManager() {
        playlistUpdateLiveData = new MutableLiveData<>();
    }

    public static synchronized PlaylistSyncManager getInstance() {
        if (instance == null) {
            instance = new PlaylistSyncManager();
        }
        return instance;
    }

    /**
     * Notify observers that playlist has been updated
     * @param screenId The screen ID that was updated
     * @param adsCount Number of ads in updated playlist
     */
    public void notifyPlaylistUpdated(String screenId, int adsCount) {
        PlaylistUpdate update = new PlaylistUpdate(screenId, adsCount, System.currentTimeMillis());
        playlistUpdateLiveData.postValue(update);
        android.util.Log.i("PlaylistSyncManager", "📡 Playlist update broadcast: screenId=" + screenId + ", adsCount=" + adsCount);
    }

    /**
     * Get LiveData for observing playlist updates
     * @return LiveData that emits PlaylistUpdate events
     */
    public LiveData<PlaylistUpdate> getPlaylistUpdateLiveData() {
        return playlistUpdateLiveData;
    }

    /**
     * Data class representing a playlist update event
     */
    public static class PlaylistUpdate {
        public final String screenId;
        public final int adsCount;
        public final long timestamp;

        public PlaylistUpdate(String screenId, int adsCount, long timestamp) {
            this.screenId = screenId;
            this.adsCount = adsCount;
            this.timestamp = timestamp;
        }
    }
}

