package com.adjaba.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

public class ImpressionRetryWorker extends Worker {

    private static final String BASE_URL = "https://api.buyir.uk/";

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
        for (ImpressionEntity impression : pending) {
            if (sendImpression(impression, token)) {
                db.impDao().deleteAdById(impression.impressionId);
            } else {
                allSent = false;
            }
        }

        return allSent ? Result.success() : Result.retry();
    }

    private boolean sendImpression(ImpressionEntity impression, String token) {
        try {
            URL url = new URL(BASE_URL + "create_impression");
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
            json.put("amountSettled", impression.amountSettled);
            json.put("contractId", impression.contractId);
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
            json.put("tags", new JSONArray(impression.tags != null ? impression.tags : new ArrayList<>()));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
