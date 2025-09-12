package com.rnd.others;

import android.content.Context;
import android.util.Log;


import com.rnd.room.AdDatabase;
import com.rnd.utilities.AuthManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIImpression {
    private static final String BASE_URL = "https://api.buyir.uk/";

    public static void sendImpression(Context context, com.rnd.room.ImpressionEntity impression) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "create_impression");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + AuthManager.getToken(context));
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                try {
                    json.put("impressionId", impression.impressionId);
                    json.put("advertId", impression.advertId);
                    json.put("amountSettled", impression.amountSettled);
                    json.put("contractId", impression.contractId);
                    json.put("currency", impression.currency);
                    json.put("dayHour", impression.dayHour);
                    json.put("playSec", impression.playSec);

                    // female counts
                    json.put("female20", impression.female20);
                    json.put("female32", impression.female32);
                    json.put("female40", impression.female40);
                    json.put("female50", impression.female50);
                    json.put("female50plus", impression.female50plus);

                    // male counts
                    json.put("male20", impression.male20);
                    json.put("male32", impression.male32);
                    json.put("male40", impression.male40);
                    json.put("male50", impression.male50);
                    json.put("male50plus", impression.male50plus);

                    json.put("format", impression.format);
                    json.put("impressionCost", impression.impressionCost);
                    json.put("isActiveContract", impression.isActiveContract);
                    json.put("locationType", impression.locationType);
                    json.put("maxBid", impression.maxBid);

                    // arrays
                    if (impression.objectDetected != null) {
                        json.put("objectDetected", new JSONArray(impression.objectDetected));
                    }
                    if (impression.tags != null) {
                        json.put("tags", new JSONArray(impression.tags));
                    }
                    if (impression.textDetected != null) {
                        json.put("textDetected", new JSONArray(impression.textDetected));
                    }

                    json.put("orientation", impression.orientation);
                    json.put("playTimeStamp", impression.playTimeStamp);
                    json.put("screenDevice", impression.screenDevice);
                    json.put("screenPlayer", impression.screenPlayer);
                    json.put("screenId", impression.screenId);

                    json.put("viewCount", impression.viewCount);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

