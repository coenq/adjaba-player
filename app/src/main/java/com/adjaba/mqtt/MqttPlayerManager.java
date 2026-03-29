package com.adjaba.mqtt;

import android.content.Context;
import android.util.Log;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;


public class MqttPlayerManager {

    private MqttAndroidClient mqttClient;
    private final Context context;
    private final String TAG = "MqttManager";

    private static final String SERVER_URI = "tcp://api.adjaba.in:1883";
    private static final String USERNAME = "adjaba-mqtt-2026";
    private static final String PASSWORD = "Adjaba@1234";

    // Callbacks
    public interface ConnectionListener {
        void onConnectionStatus(boolean isConnected);
    }

    public interface MessageListener {
        void onMessageReceived(String topic, String message);
    }

    private ConnectionListener connectionListener;
    private MessageListener messageListener;

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public MqttPlayerManager(Context context) {
        this.context = context;
    }

    public void connect(String clientId) {
        try {
            mqttClient = new MqttAndroidClient(context, SERVER_URI, clientId, Ack.AUTO_ACK,
                    new MemoryPersistence(),false,0);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.w(TAG, "Connection lost: " + (cause != null ? cause.getMessage() : ""));
                    if (connectionListener != null) {
                        connectionListener.onConnectionStatus(false);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    Log.d(TAG, "Message received - Topic: " + topic + ", Message: " + payload);
                    if (messageListener != null) {
                        messageListener.onMessageReceived(topic, payload);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivered");
                }
            });

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "✅ Connected to MQTT broker");
                    if (connectionListener != null) {
                        subscribe("l169889", 1);
                        connectionListener.onConnectionStatus(true);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "❌ Connection failed: " + exception.getMessage(), exception);
                    if (connectionListener != null) {
                        connectionListener.onConnectionStatus(false);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error connecting: " + e.getMessage(), e);
        }
    }

    public void subscribe(String storeId, int qos) {
        if (mqttClient == null) return;

        String topic = "playlist/"+storeId;

        try {
            mqttClient.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d(TAG, "✅ Subscribed to: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "❌ Subscription failed: " + exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error subscribing: " + e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                Log.d(TAG, "Disconnected from MQTT broker");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage(), e);
        }
    }

    public static SSLSocketFactory getUnsafeSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

