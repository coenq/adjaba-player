package com.adjaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthManager {

    private static final String ENCRYPTED_PREFS   = "auth";
    private static final String PLAIN_PREFS        = "auth_fallback";
    private static final String KEY_TOKEN          = "token";
    private static final String KEY_TOKEN_TIMESTAMP = "token_timestamp";
    private static final String KEY_USER_ID        = "userId";
    private static final String KEY_PASSWORD       = "password";
    private static final String TAG                = "AuthManager";

    /** 6 days in ms — refresh before the 7-day JWT expires */
    private static final long TOKEN_REFRESH_THRESHOLD_MS = 6L * 24 * 60 * 60 * 1000;

    // ── Token storage ──────────────────────────────────────────────────────────

    public static void saveToken(Context context, String token) {
        Context appCtx = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx, ENCRYPTED_PREFS, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            prefs.edit().putString(KEY_TOKEN, token).apply();
        } catch (Exception e) {
            Log.w(TAG, "Keystore unavailable, using plain storage: " + e.getMessage());
            appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_TOKEN, token).apply();
        }
    }

    public static String getToken(Context context) {
        Context appCtx = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx, ENCRYPTED_PREFS, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            String token = prefs.getString(KEY_TOKEN, null);
            if (token != null) return token;
        } catch (Exception e) {
            Log.w(TAG, "Keystore read failed, checking plain storage: " + e.getMessage());
        }
        return appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    // ── Token timestamp ────────────────────────────────────────────────────────

    /** Call immediately after saveToken() to record when the token was issued. */
    public static void saveTokenTimestamp(Context context) {
        context.getApplicationContext()
                .getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                .edit().putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis()).apply();
    }

    /**
     * Returns true if the stored token is older than 6 days, meaning it should
     * be refreshed before it expires at 7 days.
     */
    public static boolean isTokenNearExpiry(Context context) {
        long saved = context.getApplicationContext()
                .getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_TOKEN_TIMESTAMP, 0L);
        if (saved == 0L) return false; // no timestamp yet — token might be from old version
        return (System.currentTimeMillis() - saved) >= TOKEN_REFRESH_THRESHOLD_MS;
    }

    // ── Credential storage (for background re-auth) ────────────────────────────

    /** Saves userId + password encrypted so background workers can re-authenticate. */
    public static void saveCredentials(Context context, String userId, String password) {
        Context appCtx = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx, ENCRYPTED_PREFS, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            prefs.edit()
                    .putString(KEY_USER_ID, userId)
                    .putString(KEY_PASSWORD, password)
                    .apply();
            Log.i(TAG, "✅ Credentials saved for background re-auth");
        } catch (Exception e) {
            Log.w(TAG, "Keystore unavailable for credentials, using plain storage: " + e.getMessage());
            appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_USER_ID, userId)
                    .putString(KEY_PASSWORD, password)
                    .apply();
        }
    }

    /**
     * Returns saved credentials as [userId, password], or null if not stored.
     * Background workers call this to re-authenticate without user interaction.
     */
    public static String[] getCredentials(Context context) {
        Context appCtx = context.getApplicationContext();
        String userId = null;
        String password = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx, ENCRYPTED_PREFS, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            userId   = prefs.getString(KEY_USER_ID, null);
            password = prefs.getString(KEY_PASSWORD, null);
        } catch (Exception e) {
            Log.w(TAG, "Keystore read failed for credentials, checking plain storage: " + e.getMessage());
        }
        if (userId == null) {
            SharedPreferences plain = appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE);
            userId   = plain.getString(KEY_USER_ID, null);
            password = plain.getString(KEY_PASSWORD, null);
        }
        if (userId == null || password == null) return null;
        return new String[]{userId, password};
    }

    // ── Synchronous re-authentication (safe to call on background threads) ─────

    /**
     * Synchronously re-authenticates using stored credentials.
     * Saves the new token + timestamp internally on success.
     *
     * @return The new JWT token string, or null if re-auth failed.
     *         MUST NOT be called on the main/UI thread.
     */
    public static String reAuthenticateSync(Context context) {
        String[] creds = getCredentials(context);
        if (creds == null) {
            Log.e(TAG, "❌ Cannot re-authenticate: no saved credentials");
            return null;
        }

        String userId   = creds[0];
        String password = creds[1];
        Log.i(TAG, "🔄 Attempting background re-authentication for user: " + userId);

        try {
            URL url = new URL(Config.BASE_URL + "/v2/authenticate_user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            JSONObject body = new JSONObject();
            body.put("userId", userId);
            body.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                java.io.InputStream is = conn.getInputStream();
                byte[] buf = new byte[4096];
                int read;
                StringBuilder sb = new StringBuilder();
                while ((read = is.read(buf)) != -1) {
                    sb.append(new String(buf, 0, read, "UTF-8"));
                }
                is.close();
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                String newToken = json.optString("loginToken", null);
                if (newToken != null && !newToken.isEmpty()) {
                    saveToken(context, newToken);
                    saveTokenTimestamp(context);
                    Log.i(TAG, "✅ Re-authentication successful — new token saved");
                    return newToken;
                }
            }
            conn.disconnect();
            Log.e(TAG, "❌ Re-authentication failed — HTTP " + code);
        } catch (Exception e) {
            Log.e(TAG, "❌ Re-authentication exception: " + e.getMessage());
        }
        return null;
    }
}
