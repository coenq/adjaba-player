package com.adjaba.activities.viewmodel;

import android.content.Context;

import com.adjaba.room.AdDatabase;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.AuthManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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
                json.put("contractId", impression.currency);
                json.put("duration", 5);
                json.put("currency", "USD");
                json.put("dayHour", impression.dayHour);
                json.put("female20", 0);
                json.put("female32", 0);
                json.put("female40", 0);
                json.put("female50", 0);
                json.put("female50plus", 0);
                json.put("male20", 0);
                json.put("male32", 0);
                json.put("male40", 0);
                json.put("male50", 0);
                json.put("male50plus", 0);
                json.put("objectdetected", "");
                json.put("impressioncost", 0);
                json.put("isactivecontract", 0);
                json.put("textdetected", "");
                json.put("totalview", 0);
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
                    AdDatabase adDatabase=AdDatabase.getInstance(context);
                    adDatabase.impDao().deleteAdById(impression.impressionId);
                } else {
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

