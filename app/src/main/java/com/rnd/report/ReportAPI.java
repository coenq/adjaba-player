package com.rnd.report;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rnd.newmodels.ScreenRecord;
import com.rnd.utilities.AuthManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReportAPI {
    private static final String BASE_URL = "https://api.buyir.uk/";

    public static void sendImpression(Context context, ScreenRecord impression) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "create_reportview");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + AuthManager.getToken(context));
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                try {
                    json.put("screenId", impression.getScreenId());
                    json.put("screenViewId", impression.getScreenViewId());
                    json.put("amountSettled", impression.isAmountSettled());
                    json.put("currency", impression.getCurrency());
                    json.put("dayHour", impression.getDayHour());
                    json.put("playSec", impression.getPlaySec());
                    json.put("female20", impression.getFemale20());
                    json.put("female32", impression.getFemale32());
                    json.put("female40", impression.getFemale40());
                    json.put("female50", impression.getFemale50());
                    json.put("female50plus", impression.getFemale50plus());
                    json.put("format", impression.getFormat());
                    json.put("impressionCost", impression.getImpressionCost());
                    json.put("locationType", impression.getLocationType());
                    json.put("male20", impression.getMale20());
                    json.put("male32", impression.getMale32());
                    json.put("male40", impression.getMale40());
                    json.put("male50", impression.getMale50());
                    json.put("male50plus", impression.getMale50plus());
                    json.put("objectDetected", impression.getObjectDetected() != null ? new JSONArray(impression.getObjectDetected()) : new JSONArray());
                    json.put("orientation", impression.getOrientation());
                    json.put("recordDate", impression.getRecordDate());
                    json.put("recordTime", impression.getRecordTime());
                    json.put("screenDevice", impression.getScreenDevice());
                    json.put("screenPlayer", impression.getScreenPlayer());
                    json.put("sentiemtCount", impression.getSentiemtCount() != null ? new JSONArray(impression.getSentiemtCount()) : new JSONArray());
                    json.put("textDetected", impression.getTextDetected() != null ? impression.getTextDetected() : JSONObject.NULL);
                    json.put("viewCount", impression.getViewCount());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Toast.makeText(context, "Successfully Report Uploading", Toast.LENGTH_SHORT).show();
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

