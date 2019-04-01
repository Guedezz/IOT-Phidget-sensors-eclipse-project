package servo.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {

	//public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
	// Alternative broker
	public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";

	public static final String userid = "16022694"; // my Student ID
	String clientId = userid + "-sub";

	public static MqttClient mqttClient;

	public Subscriber() {

		try {
			mqttClient = new MqttClient(BROKER_URL, clientId);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start() {
		try {
			mqttClient.setCallback(new SubscribeCallback());
			if (!mqttClient.isConnected()) {
				mqttClient.connect();
			}
			// Subscribe to correct topic
			final String topic = userid + "/openRequest";
			mqttClient.subscribe(topic);
			System.out.println("Successfully subscribed! Listening to topic: " + topic);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}