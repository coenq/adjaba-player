package com.adjaba.activities.viewmodel;

import android.content.Context;
import android.util.Log;

import com.adjaba.room.AdDatabase;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.AuthManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class APIImpression {
    private static final String BASE_URL = "https://api.buyir.uk/";

    public static void sendImpression(Context context, ImpressionEntity impression) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "create_impression");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + AuthManager.getToken(context));
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("impressionId", impression.impressionId);
                json.put("advertId", impression.advertId);
                json.put("amountSettled", impression.amountSettled);
                json.put("contractId", impression.contractId);
                json.put("currency", impression.currency);
                json.put("dayHour", impression.dayHour);
                json.put("playSec", impression.playSec);
                json.put("format", impression.format);
                json.put("locationType", impression.locationType);
                json.put("maxBid", impression.maxBid);
                json.put("orientation", impression.orientation);
                json.put("playTimeStamp", impression.playTimeStamp);
                json.put("screenDevice", impression.screenDevice);
                json.put("screenPlayer", impression.screenPlayer);
                json.put("screenId", impression.screenId);
                json.put("tags", new JSONArray(impression.tags));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d("SayedAPI", "Impression sent successfully");
                    AdDatabase adDatabase=AdDatabase.getInstance(context);
                    adDatabase.impDao().deleteAdById(impression.impressionId);
                } else {
                    Log.e("API", "Failed to send impression: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

