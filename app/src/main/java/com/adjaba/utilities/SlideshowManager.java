package com.adjaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

import com.adjaba.BuildConfig;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.SlideshowImageDao;
import com.adjaba.room.SlideshowImageEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Cloud Slideshow — an optional, self-contained content type independent of the CMS/ad
 * backend. A user points it at a *publicly shared* Google Drive folder; images are cached
 * locally and mixed into the normal ad rotation like any other IMAGE item.
 *
 * Design constraints:
 *  - OFF by default and fully inert when disabled — must never affect the paid ad flow.
 *  - Never blocks the UI thread: rotation-building code reads an in-memory cache
 *    ({@link #getCachedSlideshowMediaModels}) populated ahead of time by {@link #sync}
 *    (called from a WorkManager background thread), not by querying Room on demand.
 *  - Image list source: Drive API v3 (reliable, needs a free API key) with a best-effort
 *    HTML-scraping fallback when no key is configured. The fallback parses an internal,
 *    undocumented Drive page structure and can break if Google changes it — see
 *    {@link #fetchFileListViaHtml}.
 */
public class SlideshowManager {
    private static final String TAG = "SlideshowManager";
    private static final String PREFS = "SlideshowPrefs";
    private static final String KEY_ENABLED = "slideshow_enabled";
    private static final String KEY_FOLDER_URL = "slideshow_folder_url";
    private static final String KEY_INTERVAL_SECONDS = "slideshow_interval_seconds";
    private static final int DEFAULT_INTERVAL_SECONDS = 5;

    /** In-memory cache of the last-synced slideshow images, as ready-to-play MediaModels.
     *  Populated by {@link #warmFromDatabase} / {@link #sync}; read synchronously and
     *  cheaply by rotation-building code — never triggers a DB query on the caller's thread. */
    private static volatile List<MediaModel> cachedMediaModels = new ArrayList<>();
    private static volatile boolean warmed = false;

    private SlideshowManager() {}

    // ── Configuration ───────────────────────────────────────────────────────

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static String getFolderUrl(Context context) {
        return prefs(context).getString(KEY_FOLDER_URL, "");
    }

    public static int getIntervalSeconds(Context context) {
        return prefs(context).getInt(KEY_INTERVAL_SECONDS, DEFAULT_INTERVAL_SECONDS);
    }

    /** Saves the slideshow settings. Does not sync — call {@link #triggerImmediateSync} after. */
    public static void saveConfig(Context context, boolean enabled, String folderUrl, int intervalSeconds) {
        prefs(context).edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putString(KEY_FOLDER_URL, folderUrl == null ? "" : folderUrl.trim())
                .putInt(KEY_INTERVAL_SECONDS, intervalSeconds > 0 ? intervalSeconds : DEFAULT_INTERVAL_SECONDS)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Extracts the Drive folder ID from any of the common share-link formats, or null if not found. */
    public static String extractFolderId(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        // e.g. https://drive.google.com/drive/folders/<ID>?usp=sharing
        Matcher m = Pattern.compile("/folders/([a-zA-Z0-9_-]+)").matcher(url);
        if (m.find()) return m.group(1);
        // e.g. https://drive.google.com/open?id=<ID>  or  ...?id=<ID>
        m = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)").matcher(url);
        if (m.find()) return m.group(1);
        // Already a bare folder ID
        if (url.trim().matches("[a-zA-Z0-9_-]{10,}")) return url.trim();
        return null;
    }

    // ── In-memory cache (safe to call from the main thread) ────────────────

    /**
     * Returns the currently cached slideshow images as MediaModels, ready to append to a
     * rotation, in a freshly randomized order (unlike ads, which play in their CMS-defined
     * order, Drive doesn't provide any curated order for photos — so each time a rotation is
     * built, e.g. on every Play press and every periodic backend/slideshow sync, the photos are
     * reshuffled for variety). Returns an empty list when disabled, not yet synced, or no
     * folder configured — callers don't need to check {@link #isEnabled} separately.
     */
    public static List<MediaModel> getCachedSlideshowMediaModels(Context context) {
        if (!isEnabled(context)) return new ArrayList<>();
        if (!warmed && Looper.myLooper() != Looper.getMainLooper()) {
            // First read since process start and we're off the main thread — safe to warm now.
            warmFromDatabase(context);
        }
        List<MediaModel> result = new ArrayList<>(cachedMediaModels);
        java.util.Collections.shuffle(result);
        return result;
    }

    /** How many slideshow photos play before an ad is inserted back into the rotation. */
    private static final int SLIDES_PER_AD = 10;

    /**
     * Mixes ads back into the slideshow instead of leaving them frontloaded once at the start
     * of a long run of photos: one ad plays after every {@link #SLIDES_PER_AD} photos, cycling
     * through the available ads round-robin so all of them still get airtime. If there are no
     * ads, or no photos, this is just a concatenation (nothing to interleave) — so ad-only and
     * slideshow-only screens are unaffected.
     */
    public static List<MediaModel> interleave(List<MediaModel> ads, List<MediaModel> slideshowImages) {
        if (ads.isEmpty() || slideshowImages.isEmpty()) {
            List<MediaModel> combined = new ArrayList<>(ads);
            combined.addAll(slideshowImages);
            return combined;
        }
        List<MediaModel> result = new ArrayList<>();
        int adIndex = 0;
        int count = 0;
        for (MediaModel image : slideshowImages) {
            result.add(image);
            count++;
            if (count == SLIDES_PER_AD) {
                result.add(ads.get(adIndex % ads.size()));
                adIndex++;
                count = 0;
            }
        }
        // Trailing photos that didn't reach a full group of SLIDES_PER_AD still get an ad,
        // so a short slideshow (under 10 photos) isn't left with zero ads in the rotation.
        if (count > 0) {
            result.add(ads.get(adIndex % ads.size()));
        }
        return result;
    }

    /**
     * Deletes all downloaded slideshow photos and their cache records — mirrors what logout
     * already does to the ad cache ({@code adDao().deleteAllAds()}). Call on explicit logout so
     * the next sign-in re-downloads everything fresh; offline playback between sign-ins is
     * untouched since this is never called except on logout. Leaves the enabled/folder
     * URL/interval settings alone (same as ads: screen/orientation settings also survive
     * logout) — only the downloaded media is wiped. MUST be called off the main thread.
     */
    public static void clearCache(Context context) {
        SlideshowImageDao dao = AdDatabase.getInstance(context).slideshowImageDao();
        for (SlideshowImageEntity row : dao.getAllImages()) {
            if (row.localPath != null) {
                //noinspection ResultOfMethodCallIgnored
                new File(row.localPath).delete();
            }
        }
        dao.deleteAll();
        // Also remove the slideshow root dir itself (covers any stray files not tracked in
        // Room, e.g. from an interrupted download).
        deleteRecursively(new File(context.getFilesDir(), "slideshow"));
        cachedMediaModels = new ArrayList<>();
        warmed = true;
        Log.i(TAG, "Cleared slideshow cache on logout");
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteRecursively(child);
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Loads the last-synced images from Room into the in-memory cache. MUST be called off the
     * main thread. Safe to call repeatedly (e.g. on app start, and after every sync).
     */
    public static void warmFromDatabase(Context context) {
        String folderId = extractFolderId(getFolderUrl(context));
        if (folderId == null) {
            cachedMediaModels = new ArrayList<>();
            warmed = true;
            return;
        }
        int intervalSeconds = getIntervalSeconds(context);
        SlideshowImageDao dao = AdDatabase.getInstance(context).slideshowImageDao();
        List<SlideshowImageEntity> rows = dao.getImagesForFolder(folderId);
        List<MediaModel> models = new ArrayList<>();
        for (SlideshowImageEntity row : rows) {
            if (row.localPath != null && new File(row.localPath).exists()) {
                models.add(MediaModel.createSlideshowImage(row.fileId, row.localPath, intervalSeconds));
            }
        }
        cachedMediaModels = models;
        warmed = true;
        Log.i(TAG, "Warmed slideshow cache: " + models.size() + " image(s) for folder " + folderId);
    }

    // ── Sync (network) — call only from a background thread/Worker ─────────

    /**
     * Fetches the folder's current image list, downloads new/changed images, prunes ones no
     * longer in the folder, and refreshes the in-memory cache. Safe to call when disabled or
     * unconfigured (no-ops). Never throws — logs and returns on any failure so a flaky network
     * or a Drive layout change can't affect ad playback.
     *
     * @return true if the sync completed (even if there was nothing to do); false on failure.
     */
    private static final java.util.concurrent.atomic.AtomicBoolean syncInProgress =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    public static boolean sync(Context context) {
        if (!isEnabled(context)) return true;
        // The periodic worker (every 30 min) and an immediate sync (triggered by pressing Play)
        // are two independently-scheduled WorkManager requests with different unique names, so
        // WorkManager itself won't dedupe them — without this guard they can run concurrently
        // and redundantly re-download the same files in parallel (observed on device: two
        // worker threads downloading the same 50 images at once).
        if (!syncInProgress.compareAndSet(false, true)) {
            Log.i(TAG, "Sync already in progress — skipping duplicate run");
            return true;
        }
        try {
            return doSync(context);
        } finally {
            syncInProgress.set(false);
        }
    }

    private static boolean doSync(Context context) {
        String folderId = extractFolderId(getFolderUrl(context));
        if (folderId == null) {
            Log.w(TAG, "Slideshow enabled but no valid Drive folder URL configured — skipping sync");
            return true;
        }

        List<DriveFile> files = fetchFileList(folderId);
        if (files == null) {
            Log.w(TAG, "Could not fetch Drive folder listing — keeping existing cache");
            return false;
        }

        SlideshowImageDao dao = AdDatabase.getInstance(context).slideshowImageDao();
        File folderDir = new File(new File(context.getFilesDir(), "slideshow"), folderId);
        if (!folderDir.exists()) folderDir.mkdirs();

        List<String> currentFileIds = new ArrayList<>();
        for (DriveFile file : files) {
            currentFileIds.add(file.id);
            SlideshowImageEntity existing = dao.getByFileId(file.id);
            boolean unchanged = existing != null
                    && existing.localPath != null
                    && new File(existing.localPath).exists()
                    && existing.modifiedTime != null
                    && existing.modifiedTime.equals(file.modifiedTime);
            if (unchanged) continue; // already cached, nothing changed — skip re-download

            String localPath = downloadImage(context, file, folderDir);
            if (localPath != null) {
                dao.insertImage(new SlideshowImageEntity(file.id, folderId, localPath,
                        file.modifiedTime, System.currentTimeMillis()));
                Log.i(TAG, "Cached slideshow image: " + file.name);
            } else {
                Log.w(TAG, "Failed to download slideshow image: " + file.name);
            }
        }

        // Prune images removed from the Drive folder (only when the fetch above genuinely
        // succeeded — files == null already returned early on failure, so reaching here means
        // `files` is an authoritative, if possibly empty, listing).
        for (String cachedId : dao.getFileIdsForFolder(folderId)) {
            if (!currentFileIds.contains(cachedId)) {
                SlideshowImageEntity removed = dao.getByFileId(cachedId);
                if (removed != null && removed.localPath != null) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(removed.localPath).delete();
                }
                dao.deleteByFileId(cachedId);
                Log.i(TAG, "Removed slideshow image no longer in Drive folder: " + cachedId);
            }
        }

        warmFromDatabase(context);

        // Tell any already-running playback screen to rebuild its rotation and pick up the
        // newly synced photos — otherwise a screen that started playing before this sync
        // finished would never see them until the next full Play press. Reuses the same
        // LiveData channel AdSyncWorker uses for ad updates; both AdvertWatching and
        // AdvertLandWatch already include cached slideshow images when they reload.
        String screenId = com.adjaba.workers.AdSyncWorker.getCurrentScreenId(context);
        if (screenId != null && !screenId.isEmpty()) {
            PlaylistSyncManager.getInstance().notifyPlaylistUpdated(screenId, cachedMediaModels.size());
        }
        return true;
    }

    // ── Drive fetch: API first, HTML scraping fallback ──────────────────────

    private static class DriveFile {
        String id, name, modifiedTime;
        DriveFile(String id, String name, String modifiedTime) {
            this.id = id; this.name = name; this.modifiedTime = modifiedTime;
        }
    }

    /** @return the folder's image files, or null if both the API and the HTML fallback failed. */
    private static List<DriveFile> fetchFileList(String folderId) {
        if (BuildConfig.DRIVE_API_KEY != null && !BuildConfig.DRIVE_API_KEY.isEmpty()) {
            List<DriveFile> viaApi = fetchFileListViaApi(folderId);
            if (viaApi != null) return viaApi;
            Log.w(TAG, "Drive API fetch failed — falling back to HTML parsing");
        }
        return fetchFileListViaHtml(folderId);
    }

    private static List<DriveFile> fetchFileListViaApi(String folderId) {
        OkHttpClient client = new OkHttpClient();
        String q = "'" + folderId + "' in parents and trashed=false and "
                + "(mimeType contains 'image/')";
        String url = "https://www.googleapis.com/drive/v3/files"
                + "?q=" + urlEncode(q)
                + "&fields=" + urlEncode("files(id,name,mimeType,modifiedTime)")
                + "&pageSize=1000"
                + "&key=" + BuildConfig.DRIVE_API_KEY;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Drive API HTTP " + response.code());
                return null;
            }
            JSONObject json = new JSONObject(response.body().string());
            JSONArray filesArray = json.optJSONArray("files");
            List<DriveFile> result = new ArrayList<>();
            if (filesArray != null) {
                for (int i = 0; i < filesArray.length(); i++) {
                    JSONObject f = filesArray.getJSONObject(i);
                    result.add(new DriveFile(f.getString("id"), f.optString("name", ""),
                            f.optString("modifiedTime", "")));
                }
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Drive API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Best-effort fallback with no API key: parses the public folder page's accessible DOM
     * markup. Each file row carries a {@code data-id="<fileId>"} attribute, and nested inside
     * that row is an {@code aria-label="<fileName> Image Shared"} element (added for screen
     * readers). This is undocumented, internal Drive page structure — it can stop working if
     * Google changes the page — so it exists only as a fallback; prefer configuring
     * DRIVE_API_KEY for production use. Verified against a real public folder July 2026.
     */
    private static List<DriveFile> fetchFileListViaHtml(String folderId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://drive.google.com/drive/folders/" + folderId)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Drive folder page HTTP " + response.code());
                return null;
            }
            String html = response.body().string();
            List<DriveFile> result = new ArrayList<>();

            Pattern idPattern = Pattern.compile("data-id=\"([a-zA-Z0-9_-]{15,})\"");
            Pattern imageLabelPattern = Pattern.compile("aria-label=\"([^\"]+?)\\s+Image Shared\"");
            Matcher idMatcher = idPattern.matcher(html);

            int prevEnd = -1;
            String prevFileId = null;
            while (idMatcher.find()) {
                if (prevFileId != null) {
                    addIfImageRow(result, prevFileId, html.substring(prevEnd, idMatcher.start()), imageLabelPattern);
                }
                prevFileId = idMatcher.group(1);
                prevEnd = idMatcher.end();
            }
            if (prevFileId != null) {
                addIfImageRow(result, prevFileId, html.substring(prevEnd), imageLabelPattern);
            }

            if (result.isEmpty()) {
                Log.w(TAG, "HTML fallback found no images — folder may not be public, or Drive's page structure changed");
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Drive HTML fallback error: " + e.getMessage());
            return null;
        }
    }

    /** Looks for an "<name> Image Shared" aria-label within one file row's HTML chunk and, if found, records it. */
    private static void addIfImageRow(List<DriveFile> result, String fileId, String rowChunk, Pattern imageLabelPattern) {
        Matcher m = imageLabelPattern.matcher(rowChunk);
        if (m.find()) {
            result.add(new DriveFile(fileId, m.group(1), ""));
        }
    }

    private static String downloadImage(Context context, DriveFile file, File folderDir) {
        String downloadUrl = (BuildConfig.DRIVE_API_KEY != null && !BuildConfig.DRIVE_API_KEY.isEmpty())
                ? "https://www.googleapis.com/drive/v3/files/" + file.id + "?alt=media&key=" + BuildConfig.DRIVE_API_KEY
                : "https://drive.google.com/uc?export=download&id=" + file.id;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Download HTTP " + response.code() + " for " + file.name);
                return null;
            }
            String ext = extensionFor(file.name);
            File outFile = new File(folderDir, file.id + "." + ext);
            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Download error for " + file.name + ": " + e.getMessage());
            return null;
        }
    }

    private static String extensionFor(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (ext.matches("jpg|jpeg|png|gif|webp")) return ext;
        }
        return "jpg";
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
