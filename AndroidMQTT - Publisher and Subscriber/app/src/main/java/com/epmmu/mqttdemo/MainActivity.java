package com.epmmu.mqttdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //BROKERS
    //public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";

    Handler handler = new Handler();
    Button openDoorBtn;
    Button checkRecordsBtn;
    Spinner spinner;

    static Gson gson = new Gson();

    TagData oneTag = new TagData("unknown");
    String userid = "16022694";
    String doorname = new String();
    String tagvalue = userid + "android";
    int doorid = 1;

    String screenUpdate = new String();
    int selectedColour;

    /***Tag credentials for testing - uncomment to test***/
    //int tagid = Integer.parseInt(userid); String debugString = "001"; // opens doorid 1 (name:001)
    //int tagid = 1; String debugString = "002"; //opens doorid 2 (name: 002)
    //int tagid = 2; String debugString = "001"; //opens doorid 1 (name: 001)
    int tagid = 3; String debugString = "e127"; //opens doorid 380854 (name: e127)
    //int tagid = 4; String debugString = "c205"; //opens doorid 380854 (name: c205)

    //Generate a unique Client id.
    String clientId = userid + "-android";

    //MQTT topics
    String topicname = userid + "/openRequest";
    String doorStatusTopic = userid + "/doorStatus";

    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Spinner dropdown menu to select the names of the doors to open
        spinner = (Spinner) findViewById(R.id.doorSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.doors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Debug message to be seen on android app
        TextView debugTV = (TextView) findViewById(R.id.debugTV);
        debugTV.setText("You are user " + tagid + ". You are authorished to open " + debugString);

        openDoorBtn = (Button) findViewById(R.id.openDoorBtn);
        openDoorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("PUBLISHING");
                runOnUiThread(new Runnable() {
                    public void run() {
                        handleDoorBtn();
                    }
                });
            }
        });

        //Clicking on this button will start a new activity
        checkRecordsBtn = (Button) findViewById(R.id.checkRecordsBtn);
        checkRecordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QueryActivity.class);
                startActivity(intent);
            }
        });

        // Create MQTT client and start subscribing to message queue
        try {
            // change from original. Messages in "null" are not stored
            mqttClient = new MqttClient(BROKER_URL, clientId, null);
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    //This is called when the connection is lost. We could reconnect here.
                }

                @Override
                public void messageArrived(final String topic, MqttMessage message) throws Exception {
                    //construct TagData
                    oneTag = gson.fromJson(message.toString(), TagData.class);

                    System.out.println("DEBUG: Message arrived. Topic: " + topic + "  Message: Tag ID" + oneTag.getDoorid());

                    //update text and colour according to tag being valid or not
                    if (!oneTag.getTagvalue().equals("Not authorised")) {
                        screenUpdate = "Door " + oneTag.getDoorname() + " is now open";
                        selectedColour = Color.rgb(25, 153, 22);
                    } else {
                        screenUpdate = "Invalid attempt. User: " + oneTag.getTagid() + " is not authorised to open: " + oneTag.getDoorname();
                        selectedColour = Color.RED;
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            System.out.println("Updating UI");
                            // Update UI elements
                            final TextView doorValueTV = (TextView) findViewById(R.id.doorValueTV);
                            //Change color and update text
                            doorValueTV.setTextColor(selectedColour);
                            doorValueTV.setText(screenUpdate);
                            //give it a few senconds for door to close and then update text back to closed
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    doorValueTV.setTextColor(Color.RED);
                                    doorValueTV.setText("CLOSED");
                                }
                            }, 5000);
                        }
                    });
                    if ((topicname + "/LWT").equals(topic)) {
                        System.err.println("Sensor gone!");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //no-op
                }

                @Override
                public void connectComplete(boolean b, String s) {
                    //no-op
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }

        startSubscribing();

        //ThreadPolicy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void startSubscribing() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
            mqttClient.subscribe(doorStatusTopic);
            System.out.println("Subscriber is now listening to " + doorStatusTopic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //this is called when button is pressed
    private void handleDoorBtn() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
            final MqttTopic Topic = mqttClient.getTopic(topicname);
            TagData oneTag = new TagData("unknown");
            oneTag.setTagid(tagid);
            oneTag.setTagvalue(tagvalue);
            oneTag.setDoorid(doorid);
            oneTag.setDoorname(doorname);

            String messageJson = gson.toJson(oneTag);

            Topic.publish(new MqttMessage(messageJson.getBytes()));

            System.out.println("Published data. Topic: " + Topic.getName() + "  Message: " + messageJson);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Set the text on the UI
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //change value of doorid that the user wants to open depending on the door name he/she selects
        doorname = parent.getItemAtPosition(position).toString();
        switch (doorname) {
            case "001":
                doorid = 1;
                break;
            case "002":
                doorid = 2;
                break;
            case "003":
                doorid = 3;
                break;
            case "c205":
                doorid = 63654;
                break;
            case "e127":
                doorid = 380854;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}