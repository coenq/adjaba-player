package com.adjaba.workers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.Config;
import com.adjaba.utilities.RetrofitBuilder;
import com.adjaba.utilities.PlaylistSyncManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Background worker that periodically syncs the ad playlist with the backend.
 * - Compares backend playlist with local database
 * - Downloads new ads
 * - Deletes removed ads and their media files
 * - Handles offline gracefully (skips sync if network unavailable)
 * - Broadcasts update notification via PlaylistSyncManager
 */
public class AdSyncWorker extends Worker {


    private static final String PREFS_NAME = "AdSyncPrefs";
    private static final String KEY_SCREEN_ID = "current_screen_id";

    public AdSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        android.util.Log.i("AdSyncWorker", " Starting periodic ad sync...");

        // Get current screen ID from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String screenId = prefs.getString(KEY_SCREEN_ID, null);

        if (screenId == null || screenId.isEmpty()) {
            android.util.Log.w("AdSyncWorker", "⚠️ No screen ID configured - skipping sync");
            return Result.success();
        }

        android.util.Log.i("AdSyncWorker", "   Screen ID: " + screenId);

        // Get auth token
        String token = AuthManager.getToken(context);
        if (token == null || token.isEmpty()) {
            android.util.Log.w("AdSyncWorker", "⚠️ No auth token - skipping sync");
            return Result.retry();
        }

        // Perform smart sync
        boolean syncSuccess = performSmartSync(context, screenId, token);

        return syncSuccess ? Result.success() : Result.retry();
    }

    /**
     * Performs differential sync between backend playlist and local database
     */
    private boolean performSmartSync(Context context, String screenId, String token) {
        try {
            AdDatabase db = AdDatabase.getInstance(context);

            // Get existing local ad IDs
            List<String> localAdIds = db.adDao().getAdIdsByScreen(screenId);
            android.util.Log.i("AdSyncWorker", " Local database has " + (localAdIds == null ? 0 : localAdIds.size()) + " ads");

            // Fetch backend playlist
            RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
            String screenIdForApi = screenId.contains("/") ? screenId.split("/")[0] : screenId;

            Call<List<WatchingModel>> call = retrofitBuilder.apiCalls()
                    .getAdsByScreen(screenIdForApi, "Bearer " + token);

            Response<List<WatchingModel>> response = call.execute();

            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    android.util.Log.w("AdSyncWorker", "⚠️ 401 Unauthorized — refreshing token and retrying...");
                    String newToken = AuthManager.reAuthenticateSync(context);
                    if (newToken == null) {
                        android.util.Log.e("AdSyncWorker", "❌ Re-authentication failed — aborting sync");
                        return false;
                    }
                    // Retry the playlist call once with the fresh token
                    call = retrofitBuilder.apiCalls().getAdsByScreen(screenIdForApi, "Bearer " + newToken);
                    response = call.execute();
                    if (!response.isSuccessful()) {
                        android.util.Log.e("AdSyncWorker", "❌ API error after re-auth - response code: " + response.code());
                        return false;
                    }
                    android.util.Log.i("AdSyncWorker", "✅ Retry succeeded after token refresh");
                } else {
                    android.util.Log.e("AdSyncWorker", "❌ API error - response code: " + response.code());
                    return false;
                }
            }

            List<WatchingModel> backendAds = response.body();
            if (backendAds == null || backendAds.isEmpty()) {
                android.util.Log.i("AdSyncWorker", "⚠️ No ads in backend playlist - keeping local cache");
                return true;
            }

            android.util.Log.i("AdSyncWorker", " Backend has " + backendAds.size() + " ads");

            // Get backend ad IDs
            List<String> backendAdIds = new ArrayList<>();
            for (WatchingModel ad : backendAds) {
                backendAdIds.add(ad.adContractData.advertId);
            }

            // Determine NEW ads (in backend but not in local)
            List<String> newAdIds = new ArrayList<>();
            for (String backendId : backendAdIds) {
                if (!localAdIds.contains(backendId)) {
                    newAdIds.add(backendId);
                }
            }

            // Determine REMOVED ads (in local but not in backend)
            List<String> removedAdIds = new ArrayList<>();
            for (String localId : localAdIds) {
                if (!backendAdIds.contains(localId)) {
                    removedAdIds.add(localId);
                }
            }

            android.util.Log.i("AdSyncWorker", "    NEW ads to download: " + newAdIds.size());
            android.util.Log.i("AdSyncWorker", "   ️ REMOVED ads to delete: " + removedAdIds.size());
            android.util.Log.i("AdSyncWorker", "   ✅ EXISTING ads (keep): " + (localAdIds.size() - removedAdIds.size()));

            // Delete removed ads and their media files
            for (String removedId : removedAdIds) {
                AdEntity removedAd = db.adDao().getAdById(removedId);
                if (removedAd != null && removedAd.localPath != null) {
                    File mediaFile = new File(removedAd.localPath);
                    if (mediaFile.exists()) {
                        boolean deleted = mediaFile.delete();
                        android.util.Log.i("AdSyncWorker", "   ️ Deleted media file: " + mediaFile.getName() + " (success=" + deleted + ")");
                    }
                }
                db.adDao().deleteAdById(removedId);
                android.util.Log.i("AdSyncWorker", "   ️ Deleted ad from DB: " + removedId);
            }

            // Download new ads
            int downloadedCount = 0;
            for (WatchingModel ad : backendAds) {
                String advertId = ad.adContractData.advertId;

                // Skip ads already in local database
                if (!newAdIds.contains(advertId)) {
                    continue;
                }

                android.util.Log.d("AdSyncWorker", "   Downloading new ad: " + advertId);

                String videoUrl = ad.adContractData.videoUrl;
                String adFormat = ad.adContractData.format != null
                        ? ad.adContractData.format.toUpperCase() : "";

                // Non-downloadable content types: store metadata directly, no file needed
                if ("LIVE_STREAM".equals(adFormat) || "WEB_CONTENT".equals(adFormat)
                        || "SOCIAL_FEED".equals(adFormat)) {
                    // For LIVE_STREAM/WEB_CONTENT the URL is stored as localPath so it becomes
                    // media.getUrl() when MediaModel is rebuilt from the database.
                    String contentUrl = "SOCIAL_FEED".equals(adFormat) ? ""
                            : (videoUrl != null ? videoUrl : "");
                    // LIVE_STREAM and WEB_CONTENT require a URL — skip if missing
                    if (("LIVE_STREAM".equals(adFormat) || "WEB_CONTENT".equals(adFormat))
                            && contentUrl.isEmpty()) {
                        android.util.Log.w("AdSyncWorker", "  ⚠️ " + adFormat + " ad " + advertId
                                + " has no URL — skipping");
                        continue;
                    }
                    AdEntity adEntity = new AdEntity(
                            advertId, adFormat, contentUrl,
                            ad.adContractData.textTop,
                            ad.adContractData.textBottom,
                            ad.adContractData.textLeft,
                            ad.adContractData.textRight,
                            ad.duration * 1000, "Landscape", screenId,
                            ad.contractId,
                            listToString(ad.adContractData.targetHours),
                            downloadedCount, ad.currency, ad.maxBid,
                            listStrToString(ad.adContractData.targetGender),
                            listStrToString(ad.adContractData.targetAgeGroup),
                            listStrToString(ad.adContractData.targetTags),
                            listStrToString(ad.adContractData.targetEmotion),
                            ad.adContractData.streamType,
                            ad.adContractData.socialPlatform,
                            ad.adContractData.socialHashtag
                    );
                    db.adDao().insertAd(adEntity);
                    downloadedCount++;
                    android.util.Log.d("AdSyncWorker", "  ✅ Saved streaming/web/social ad: " + advertId);
                    continue;
                }

                if (videoUrl == null || videoUrl.isEmpty()) {
                    android.util.Log.w("AdSyncWorker", "  ⚠️ Ad " + advertId + " has empty path - skipping");
                    continue;
                }

                // Download media file
                String downloadUrl = Config.BASE_URL + "/media/" + videoUrl;
                String extension;
                try {
                    int dot = videoUrl.lastIndexOf('.');
                    extension = dot >= 0 ? videoUrl.substring(dot + 1) : "mp4";
                } catch (Exception e) {
                    extension = "mp4";
                }
                String fileName = UUID.randomUUID().toString() + "." + extension;

                String localPath = downloadFileWithAuth(context, downloadUrl, fileName, token);
                if (localPath != null) {
                    String mediaFormat = isVideo(videoUrl) ? "VIDEO" : "IMAGE";

                    AdEntity adEntity = new AdEntity(
                            advertId,
                            mediaFormat,
                            localPath,
                            ad.adContractData.textTop,
                            ad.adContractData.textBottom,
                            ad.adContractData.textLeft,
                            ad.adContractData.textRight,
                            ad.duration * 1000,
                            "Landscape",
                            screenId,
                            ad.contractId,
                            listToString(ad.adContractData.targetHours),
                            downloadedCount,
                            ad.currency,
                            ad.maxBid,
                            listStrToString(ad.adContractData.targetGender),
                            listStrToString(ad.adContractData.targetAgeGroup),
                            listStrToString(ad.adContractData.targetTags),
                            listStrToString(ad.adContractData.targetEmotion),
                            null, null, null
                    );

                    db.adDao().insertAd(adEntity);
                    downloadedCount++;
                    android.util.Log.d("AdSyncWorker", "  ✅ Downloaded and saved ad: " + advertId);
                }
            }

            android.util.Log.i("AdSyncWorker", "✅ Sync complete - downloaded " + downloadedCount + " new ads");

            // Broadcast update notification if changes were made
            if (downloadedCount > 0 || removedAdIds.size() > 0) {
                broadcastPlaylistUpdate(context, screenId, backendAds.size());
            }

            return true;

        } catch (Exception e) {
            android.util.Log.e("AdSyncWorker", "❌ Sync failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Download file with authorization header
     */
    private String downloadFileWithAuth(Context context, String fileUrl, String fileName, String token) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(fileUrl);
        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                InputStream inputStream = response.body().byteStream();
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            android.util.Log.e("AdSyncWorker", "  ❌ Download error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Notify activities of playlist update via LiveData
     */
    private void broadcastPlaylistUpdate(Context context, String screenId, int adsCount) {
        PlaylistSyncManager.getInstance().notifyPlaylistUpdated(screenId, adsCount);
        android.util.Log.i("AdSyncWorker", "📡 Notified playlist update via LiveData");
    }

    /**
     * Check if file is a video based on extension
     */
    private boolean isVideo(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") ||
                lower.endsWith(".webm") || lower.endsWith(".mov");
    }

    /**
     * Convert list of integers to slash-separated string
     */
    private String listToString(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    /** Convert a List<String> to a comma-separated string for DB storage. */
    private String listStrToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return android.text.TextUtils.join(",", list);
    }

    /**
     * Helper method to set current screen ID for background sync
     * Should be called from SelectScreens when Play is clicked
     */
    public static void setCurrentScreenId(Context context, String screenId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SCREEN_ID, screenId).apply();
        android.util.Log.i("AdSyncWorker", "✅ Set current screen ID for background sync: " + screenId);
    }

    /** Returns the screen ID set by {@link #setCurrentScreenId}, or null if none is configured yet. */
    public static String getCurrentScreenId(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_SCREEN_ID, null);
    }
}

