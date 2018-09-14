package com.wgt.mqtt_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_msg, et_sub, et_unsub, et_pub_topic;
    private Button btn_pub, btn_sub, btn_unsub;

    private MqttAndroidClient client;

    private String BROKER_URL = "192.168.1.173";
    private String BROKER_PORT = "1883";
    private String USERNAME = "deba";
    private String PASSWORD = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();


        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + BROKER_URL + ":" + BROKER_PORT, clientId);

        try {
            IMqttToken token = client.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connected with MQTT Broker", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Connection Failed with MQTT Broker\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "MQTT Broker Connection Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        //get published message
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(MainActivity.this, "onConnectComplete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(MainActivity.this, "onConnectionLost", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Toast.makeText(MainActivity.this, "MESSAGE: " + new String(mqttMessage.getPayload()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {

        et_pub_topic = findViewById(R.id.et_pub_topic);
        et_msg = findViewById(R.id.et_message);
        et_sub = findViewById(R.id.et_sub);
        et_unsub = findViewById(R.id.et_unsub);

        btn_pub = findViewById(R.id.btn_pub);
        btn_sub = findViewById(R.id.btn_sub);
        btn_unsub = findViewById(R.id.btn_unsub);

        btn_pub.setOnClickListener(this);
        btn_sub.setOnClickListener(this);
        btn_unsub.setOnClickListener(this);
    }


    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setWill(et_pub_topic.getText().toString(), "I am going offline".getBytes(), 1, true);
        mqttConnectOptions.setUserName(USERNAME);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());
        return mqttConnectOptions;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pub:
                try {
                    client.publish(et_pub_topic.getText().toString(), new MqttMessage(et_msg.getText().toString().getBytes("UTF-8")));
                } catch (MqttException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_sub:
                try {
                    IMqttToken subToken = client.subscribe(et_sub.getText().toString(), 1);
                    subToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken iMqttToken) {
                            Toast.makeText(MainActivity.this, "Subscribed to topic: " + et_sub.getText().toString(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                            Toast.makeText(MainActivity.this, "Failed to Subscribe", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_unsub:
                try {
                    IMqttToken unsubToken = client.unsubscribe(et_unsub.getText().toString());
                    unsubToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken iMqttToken) {
                            Toast.makeText(MainActivity.this, "Unsubscribe from Topic: " + et_unsub.getText().toString(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                            Toast.makeText(MainActivity.this, "Failed to Unsubscribe", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
