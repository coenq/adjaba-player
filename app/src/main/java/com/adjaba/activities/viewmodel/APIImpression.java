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


public class APIImpression {

    private static final String BASE_URL = "https://api.adjaba.in/";

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
                json.put("screenId", impression.screenId);
                json.put("advertId", impression.advertId);
                json.put("impressionId", impression.impressionId);
                json.put("contractId", impression.currency);
                json.put("amountSettled", impression.amountSettled);
                json.put("currency", "USD");
                json.put("dayHour", impression.dayHour);
                json.put("duration", 5);
                json.put("female20", 0);
                json.put("female32", 0);
                json.put("female40", 0);
                json.put("female50", 0);
                json.put("female50plus", 0);
                json.put("format", impression.format);
                json.put("impressioncost", 0);
                json.put("isactivecontract", 0);
                json.put("locationType", impression.locationType);
                json.put("male20", 0);
                json.put("male32", 0);
                json.put("male40", 0);
                json.put("male50", 0);
                json.put("male50plus", 0);
                json.put("maxBid", impression.maxBid);
                json.put("objectdetected", "");
                json.put("orientation", impression.orientation);
                json.put("playTimeStamp", impression.playTimeStamp);
                json.put("screenDevice", impression.screenDevice);
                json.put("screenPlayer", impression.screenPlayer);
                json.put("tags", new JSONArray(impression.tags));
                json.put("textdetected", "");
                json.put("totalview", 0);
                Log.d("sayed_uplodaa",json.toString());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {

                } else {

                }

                conn.disconnect();
            } catch (Exception e) {
                Log.d("sayed_fantasy",e.getMessage());

                e.printStackTrace();
            }
        }).start();
    }
}

