package com.adjaba.utilities;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

/**
 * Debug logger for tracking ads loading flow
 */
public class DebugLogger {
    private static final String TAG = "AdjabaDEBUG";

    public static void logStep(String step, String message) {
        String fullMsg = "[" + step + "] " + message;
        Log.i(TAG, fullMsg);
        System.out.println("=== ADJABA DEBUG: " + fullMsg);
    }

    public static void logError(String step, String error) {
        String fullMsg = "[ERROR-" + step + "] " + error;
        Log.e(TAG, fullMsg);
        System.out.println("!!! ADJABA ERROR: " + fullMsg);
    }

    public static void logError(String step, String error, Throwable t) {
        logError(step, error);
        if (t != null) {
            Log.e(TAG, "Stack trace:", t);
            t.printStackTrace();
        }
    }

    public static void showDebugToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, "DEBUG: " + message, Toast.LENGTH_LONG).show();
        }
    }

    // Track API flow
    public static void apiCallStarted(String endpoint, String screenId) {
        logStep("API_CALL", "Calling " + endpoint + " for screen: " + screenId);
    }

    public static void apiResponseReceived(int code, String message) {
        logStep("API_RESPONSE", "Code: " + code + ", Message: " + message);
    }

    public static void adsReceivedCount(int count) {
        logStep("ADS_RECEIVED", "Got " + count + " ads from API");
    }

    // Track database operations
    public static void dbQueryStarted(String query) {
        logStep("DB_QUERY", query);
    }

    public static void dbOperationComplete(String operation, int recordCount) {
        logStep("DB_COMPLETE", operation + " - " + recordCount + " records");
    }

    // Track playback flow
    public static void playbackStarting(int adCount) {
        logStep("PLAYBACK_START", "Starting with " + adCount + " ads");
    }

    public static void playingMedia(int index, String type, int duration) {
        logStep("PLAYING", "Item " + index + " - Type: " + type + " - Duration: " + duration + "s");
    }
}

