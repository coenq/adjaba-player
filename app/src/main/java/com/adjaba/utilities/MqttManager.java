package com.adjaba.utilities;

import android.content.Context;
import android.util.Log;

import com.adjaba.models.DemographicData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * MQTT Manager for real-time demographic-based ad switching.
 * Connects to Adjaba MQTT broker (EMQX) on port 8883 (TLS) and subscribes
 * to store/{screenId} topic to receive demographic data from OnlyCamera.
 *
 * NOTE on TLS: Port 1883 (plaintext) is blocked on the broker — only 8883 (TLS) is exposed.
 * A trust-all SSL factory is used as fallback for self-signed EMQX certificates.
 * Replace with a proper trust store in production if a CA-signed cert is installed.
 */
public class MqttManager {
    private static final String TAG = "MqttManager";

    // Broker connection details — EMQX, TLS only (port 1883 is blocked)
    private static final String BROKER_URL = "ssl://api.adjaba.in:8883";
    private static final String USERNAME   = "adjaba-mqtt-2026";
    private static final String PASSWORD   = "Adjaba@1234";
    private static final int    QOS               = 1;
    private static final int    KEEP_ALIVE        = 60;
    private static final int    CONNECTION_TIMEOUT = 30;

    private static MqttManager instance;
    private MqttClient mqttClient;
    private String currentTopic;
    private OnDemographicDataListener listener;
    private final Gson gson = new Gson();
    private boolean isConnected = false;

    /**
     * Listener interface for demographic data callbacks
     */
    public interface OnDemographicDataListener {
        void onDemographicDataReceived(DemographicData data);
        void onConnected();
        void onConnectionLost(Throwable cause);
    }

    private MqttManager() {}

    public static synchronized MqttManager getInstance() {
        if (instance == null) {
            instance = new MqttManager();
        }
        return instance;
    }

    /**
     * Connect to MQTT broker and subscribe to screen's demographic topic.
     * Runs on a background thread (connect() is blocking).
     *
     * @param context  Application context
     * @param screenId Screen identifier (e.g. "l169889", "Demo136")
     * @param listener Callback listener for demographic data
     */
    public void connect(Context context, String screenId, OnDemographicDataListener listener) {
        if (screenId == null || screenId.isEmpty()) {
            Log.w(TAG, "Cannot connect: screenId is null or empty");
            return;
        }

        this.listener  = listener;
        this.currentTopic = "store/" + screenId;

        new Thread(() -> {
            try {
                String clientId = "player-" + screenId + "-" + System.currentTimeMillis();
                mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.w(TAG, "MQTT connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
                        isConnected = false;
                        if (listener != null) listener.onConnectionLost(cause);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        handleIncomingMessage(topic, message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) { }
                });

                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(USERNAME);
                options.setPassword(PASSWORD.toCharArray());
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                options.setConnectionTimeout(CONNECTION_TIMEOUT);
                options.setKeepAliveInterval(KEEP_ALIVE);

                // Apply TLS socket factory for ssl:// connection
                // Uses a trust-all factory to handle self-signed EMQX certificates.
                // TODO: Replace with a CA-pinned trust store once a proper cert is deployed.
                options.setSocketFactory(buildTrustAllSSLFactory());

                Log.d(TAG, "Connecting to MQTT broker (TLS): " + BROKER_URL);
                Log.d(TAG, "Client ID: " + clientId + " | Topic: " + currentTopic);

                mqttClient.connect(options);
                Log.d(TAG, "✅ Connected to MQTT broker (TLS) successfully");
                isConnected = true;

                mqttClient.subscribe(currentTopic, QOS);
                Log.d(TAG, "✅ Subscribed to " + currentTopic + " (QoS " + QOS + ")");

                if (listener != null) listener.onConnected();

            } catch (MqttException e) {
                Log.e(TAG, "❌ MQTT connection error (code " + e.getReasonCode() + "): " + e.getMessage(), e);
                isConnected = false;
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Log.e(TAG, "❌ TLS setup error: " + e.getMessage(), e);
                isConnected = false;
            }
        }, "MqttConnectThread").start();
    }

    /**
     * Build a trust-all SSLSocketFactory.
     *
     * ⚠️ This disables certificate verification — fine for a self-signed/dev broker,
     * but should be replaced with a proper trust store in production.
     */
    private SSLSocketFactory buildTrustAllSSLFactory()
            throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                @Override public void checkClientTrusted(X509Certificate[] c, String a) { }
                @Override public void checkServerTrusted(X509Certificate[] c, String a) { }
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }

    /**
     * Handle incoming MQTT message — parse JSON and notify listener.
     */
    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            Log.d(TAG, "📨 Message on " + topic + ": " + payload);

            DemographicData data = gson.fromJson(payload, DemographicData.class);

            if (data != null && data.isValid()) {
                Log.d(TAG, "✅ Valid demographic data: " + data);
                if (listener != null) listener.onDemographicDataReceived(data);
            } else {
                Log.w(TAG, "⚠️ Skipped: customerCount=0 or missing fields");
            }

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "❌ JSON parse error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "❌ Message handling error: " + e.getMessage(), e);
        }
    }

    /**
     * Disconnect from MQTT broker and clean up.
     */
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                Log.d(TAG, "Disconnecting from MQTT broker...");
                mqttClient.disconnect();
                mqttClient.close();
                Log.d(TAG, "✅ Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            Log.e(TAG, "❌ Error disconnecting: " + e.getMessage(), e);
        } finally {
            isConnected = false;
            currentTopic = null;
            listener = null;
        }
    }

    /** @return true if currently connected to the broker */
    public boolean isConnected() {
        return isConnected && mqttClient != null && mqttClient.isConnected();
    }

    /** @return Current subscribed topic, or null if not connected */
    public String getCurrentTopic() {
        return currentTopic;
    }
}
