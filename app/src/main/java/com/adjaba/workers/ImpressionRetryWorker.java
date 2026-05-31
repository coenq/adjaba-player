package com.adjaba.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adjaba.room.AdDatabase;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.Config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImpressionRetryWorker extends Worker {

    public ImpressionRetryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AdDatabase db = AdDatabase.getInstance(context);
        List<ImpressionEntity> pending = db.impDao().getAllImpressions();

        if (pending == null || pending.isEmpty()) {
            return Result.success();
        }

        String token = AuthManager.getToken(context);
        if (token == null) {
            return Result.retry();
        }

        boolean allSent = true;
        boolean tokenRefreshed = false;

        for (ImpressionEntity impression : pending) {
            int code = sendImpression(impression, token);
            if (code == 200) {
                db.impDao().deleteAdById(impression.impressionId);
            } else {
                // On first 401, refresh the token and retry this impression once
                if (code == 401 && !tokenRefreshed) {
                    tokenRefreshed = true;
                    android.util.Log.w("ImpressionRetryWorker", "⚠️ 401 — refreshing token and retrying...");
                    String newToken = AuthManager.reAuthenticateSync(context);
                    if (newToken != null) {
                        token = newToken;
                        code = sendImpression(impression, token);
                        if (code == 200) {
                            db.impDao().deleteAdById(impression.impressionId);
                            continue;
                        }
                    }
                }
                allSent = false;
            }
        }

        return allSent ? Result.success() : Result.retry();
    }

    /**
     * Attempts to POST a single impression to the backend.
     * @return HTTP response code (200 = success), or -1 on network exception.
     */
    private int sendImpression(ImpressionEntity impression, String token) {
        try {
            URL url = new URL(Config.BASE_URL + "/create_impression");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            JSONObject json = new JSONObject();
            json.put("impressionId", impression.impressionId);
            json.put("advertId", impression.advertId);
            json.put("contractId", impression.contractId);
            json.put("screenId", impression.screenId);
            json.put("playTimeStamp", impression.playTimeStamp);
            json.put("playSec", impression.playSec);
            json.put("impressionCost", 0);
            json.put("type", "IMPRESSION");
            json.put("format", impression.format);
            json.put("locationType", impression.locationType);
            json.put("maxBid", impression.maxBid);
            json.put("orientation", impression.orientation);
            json.put("screenDevice", impression.screenDevice);
            json.put("screenPlayer", impression.screenPlayer);
            json.put("tags", new JSONArray(impression.tags != null ? impression.tags : new ArrayList<>()));
            json.put("amountSettled", impression.amountSettled);
            json.put("currency", impression.currency);
            json.put("dayHour", impression.dayHour);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
