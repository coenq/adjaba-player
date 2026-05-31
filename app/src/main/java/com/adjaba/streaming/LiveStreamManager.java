package com.adjaba.streaming;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

/**
 * Live Stream Manager for Digital Signage
 * Supports: HLS, DASH, RTSP, and progressive HTTP streams
 *
 * Use Cases:
 * - Live TV channels
 * - Security camera feeds
 * - YouTube Live streams
 * - Corporate webcasts
 * - Event broadcasts
 */
public class LiveStreamManager {

    private static final String TAG = "LiveStreamManager";

    public enum StreamType {
        HLS,        // HTTP Live Streaming (.m3u8)
        DASH,       // Dynamic Adaptive Streaming over HTTP (.mpd)
        RTSP,       // Real Time Streaming Protocol
        HTTP        // Progressive download (MP4, etc.)
    }

    public enum StreamQuality {
        AUTO,       // Adaptive bitrate
        LOW,        // 480p or lower
        MEDIUM,     // 720p
        HIGH,       // 1080p
        ULTRA       // 4K
    }

    private ExoPlayer player;
    private PlayerView playerView;
    private Context context;
    private StreamListener listener;
    private String currentStreamUrl;
    private StreamType currentStreamType;
    private boolean isLive;

    public interface StreamListener {
        void onStreamReady();
        void onStreamPlaying();
        void onStreamBuffering();
        void onStreamError(String error);
        void onStreamEnded();
    }

    public LiveStreamManager(Context context, PlayerView playerView) {
        this.context = context;
        this.playerView = playerView;
        initializePlayer();
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(context)
                .build();

        playerView.setPlayer(player);
        playerView.setUseController(false); // Hide controls for digital signage

        // Add listener for playback events
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_READY:
                        if (listener != null) listener.onStreamReady();
                        break;
                    case Player.STATE_BUFFERING:
                        if (listener != null) listener.onStreamBuffering();
                        break;
                    case Player.STATE_ENDED:
                        if (listener != null) listener.onStreamEnded();
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying && listener != null) {
                    listener.onStreamPlaying();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                String errorMsg = "Stream error: " + error.getMessage();
                Log.e(TAG, errorMsg, error);
                if (listener != null) {
                    listener.onStreamError(errorMsg);
                }
            }
        });
    }

    public void setStreamListener(StreamListener listener) {
        this.listener = listener;
    }

    /**
     * Detect stream type from URL
     */
    private StreamType detectStreamType(String url) {
        if (url == null) return StreamType.HTTP;

        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains(".m3u8")) {
            return StreamType.HLS;
        } else if (lowerUrl.contains(".mpd")) {
            return StreamType.DASH;
        } else if (lowerUrl.startsWith("rtsp://")) {
            return StreamType.RTSP;
        } else {
            return StreamType.HTTP;
        }
    }

    /**
     * Play live stream from URL
     */
    public void playStream(String streamUrl) {
        playStream(streamUrl, detectStreamType(streamUrl));
    }

    /**
     * Play live stream with explicit type
     */
    public void playStream(String streamUrl, StreamType streamType) {
        this.currentStreamUrl = streamUrl;
        this.currentStreamType = streamType;
        this.isLive = true;

        Log.i(TAG, "Playing stream: " + streamUrl + " (Type: " + streamType + ")");

        try {
            // Create appropriate media source based on stream type
            MediaSource mediaSource = createMediaSource(streamUrl, streamType);

            // Prepare and play
            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(true);

        } catch (Exception e) {
            Log.e(TAG, "Error playing stream", e);
            if (listener != null) {
                listener.onStreamError("Failed to play stream: " + e.getMessage());
            }
        }
    }

    /**
     * Create MediaSource based on stream type
     */
    private MediaSource createMediaSource(String url, StreamType streamType) {
        // Create data source factory
        DefaultHttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(30000)
                        .setReadTimeoutMs(30000);

        DefaultDataSource.Factory dataSourceFactory =
                new DefaultDataSource.Factory(context, httpDataSourceFactory);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));

        switch (streamType) {
            case HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem);

            case DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem);

            case RTSP:
            case HTTP:
            default:
                return new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem);
        }
    }

    /**
     * Play RTSP stream (security cameras, etc.)
     */
    public void playRtspStream(String rtspUrl) {
        playStream(rtspUrl, StreamType.RTSP);
    }

    /**
     * Play HLS stream (.m3u8)
     */
    public void playHlsStream(String hlsUrl) {
        playStream(hlsUrl, StreamType.HLS);
    }

    /**
     * Play DASH stream (.mpd)
     */
    public void playDashStream(String dashUrl) {
        playStream(dashUrl, StreamType.DASH);
    }

    /**
     * Play YouTube Live stream
     * Note: Requires extracting HLS URL from YouTube
     */
    public void playYouTubeLive(String youtubeUrl) {
        // TODO: Implement YouTube URL extraction
        Log.w(TAG, "YouTube Live support requires URL extraction - implement separately");
    }

    /**
     * Stop stream
     */
    public void stop() {
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }
        isLive = false;
    }

    /**
     * Pause stream (if supported)
     */
    public void pause() {
        if (player != null && !isWindowLive()) {
            player.pause();
        }
    }

    /**
     * Resume stream
     */
    public void resume() {
        if (player != null) {
            player.play();
        }
    }

    /**
     * Check if current stream is live
     */
    public boolean isLive() {
        return isLive;
    }

    /**
     * Check if current window is live
     */
    private boolean isWindowLive() {
        if (player != null) {
            return player.isCurrentMediaItemLive();
        }
        return false;
    }

    /**
     * Get current stream URL
     */
    public String getCurrentStreamUrl() {
        return currentStreamUrl;
    }

    /**
     * Get current stream type
     */
    public StreamType getCurrentStreamType() {
        return currentStreamType;
    }

    /**
     * Check if stream is playing
     */
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        if (player != null) {
            player.setVolume(Math.max(0f, Math.min(1f, volume)));
        }
    }

    /**
     * Mute stream
     */
    public void mute() {
        setVolume(0f);
    }

    /**
     * Unmute stream
     */
    public void unmute() {
        setVolume(1f);
    }

    /**
     * Release player resources
     */
    public void release() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}

