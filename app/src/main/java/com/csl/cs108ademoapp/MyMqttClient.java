package com.csl.cs108ademoapp;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class MyMqttClient {
    final boolean DEBUG = false; final String TAG = "Hello";
    boolean isMqttServerConnected = false;
    Context context;
    public MyMqttClient(Context context) {
        this.context = context;

        ///This here are the mqtt broker informations
        String clientId = MqttClient.generateClientId();
        Log.i(TAG, "MQTT: clientId is " + (clientId == null ? "null"  : clientId));
        //String serverMqttLocation = "tcp://192.168.25.153:1883";
        //String serverMqttLocation = "tcp://localhost:1883";
        String serverMqttLocation = "tcp://" + MainActivity.csLibrary4A.getServerMqttLocation() + ":1883";
        Log.i(TAG, "MQTT: serverLocation is " + serverMqttLocation);
        client = new MqttAndroidClient(context, serverMqttLocation, clientId, Ack.AUTO_ACK);
        Log.i(TAG, "MQTT: client is " + (client == null ? "null"  : "valid"));

        if (true) {
            MqttConnectOptions options = new MqttConnectOptions();
            //options.setUserName("narada");
            //options.setPassword("narada146551".toCharArray());
            //options.setPassword("narada683796".toCharArray());
            options.setKeepAliveInterval(60);//seconds
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(this, mMessageReceiver,
                    new IntentFilter(AppConstants.INTENT_FILTERS.DASHBOARD_MESSAGE),RECEIVER_NOT_EXPORTED);
        }else {
            ContextCompat.registerReceiver(this, mMessageReceiver,
                    new IntentFilter(AppConstants.INTENT_FILTERS.DASHBOARD_MESSAGE));
        }*/

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "MQTT: client.setCallback connectionLost");
                isMqttServerConnected = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "MQTT: client.setCallback messageArrived");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "MQTT: client.setCallback deliveryComplete");
            }
        });
    }
    MqttAndroidClient client;
    public void connect(View v) {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "MQTT: client.connect.setActionCallback onSuccess");
                    //Toast.makeText(MainActivity.this,"connected!!",Toast.LENGTH_LONG).show();
                    subscribe();
                    isMqttServerConnected = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "MQTT: client.connect.setActionCallback onFailure");
                    //Toast.makeText(MainActivity.this,"connection failed!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "MQTT: conn Exception: " + e.toString());
        }
    }
    public void disconnect(View v) {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isMqttServerConnected = false;
                    Log.i(TAG, "MQTT: client.disconnect.setActionCallback onSuccess");
                    //Toast.makeText(MainActivity.this,"Disconnected!!",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "MQTT: client.disconnect.setActionCallback onFailure");
                    //Toast.makeText(MainActivity.this,"Could not diconnect!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "MQTT: disconn Exception: " + e.toString());
        }
    }
    private void subscribe() {
        try{
            client.subscribe("event",0);
            Log.i(TAG, "MQTT: client.subscribe");
        } catch (Exception e){
            Log.i(TAG, "MQTT: setSubscription Exception: " + e.toString());
        }
    }
    public void publish(String message) {
        String topic = MainActivity.csLibrary4A.getTopicMqtt();
        //String message = "the payload 11";
        try {
            client.publish(topic, message.getBytes(),0,false);
            Log.i(TAG, "MQTT: client.publish");
            //Toast.makeText(this,"Published Message",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i(TAG, "MQTT: published Exception: " + e.toString());
        }
    }
}
