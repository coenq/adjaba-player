package com.adjaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class AuthManager {

    private static final String ENCRYPTED_PREFS = "auth";
    private static final String PLAIN_PREFS      = "auth_fallback";
    private static final String KEY_TOKEN        = "token";
    private static final String TAG              = "AuthManager";

    /**
     * Saves the session token. Tries hardware-backed encrypted storage first;
     * falls back to plain SharedPreferences on devices where the Android Keystore
     * is unavailable (e.g. Fire TV, Android TV boxes without a lock screen).
     */
    public static void saveToken(Context context, String token) {
        Context appCtx = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx,
                    ENCRYPTED_PREFS,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            prefs.edit().putString(KEY_TOKEN, token).apply();
        } catch (Exception e) {
            Log.w(TAG, "Keystore unavailable, using plain storage: " + e.getMessage());
            appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_TOKEN, token).apply();
        }
    }

    /**
     * Returns the saved session token, or null if none is stored.
     * Reads from encrypted storage first; if that fails or returns nothing,
     * checks the plain-storage fallback written by saveToken().
     */
    public static String getToken(Context context) {
        Context appCtx = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    appCtx,
                    ENCRYPTED_PREFS,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            String token = prefs.getString(KEY_TOKEN, null);
            if (token != null) return token;
        } catch (Exception e) {
            Log.w(TAG, "Keystore read failed, checking plain storage: " + e.getMessage());
        }
        return appCtx.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }
}
