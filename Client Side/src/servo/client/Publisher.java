package servo.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import com.google.gson.Gson;

public class Publisher {

	//public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
	public static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
	public static final String userid = "16022694"; // my student-id
	public static final String TOPIC = userid + "/doorStatus";
	static String clientId = userid + "-pub";
	private static MqttClient client;
	static int error = 0;
	static Gson gson = new Gson();
	
	public static void publish(TagData tagData) throws MqttException {
		try {
			String JsonString = gson.toJson(tagData); 
			client = new MqttClient(BROKER_URL, clientId);
			// create MQTT session
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setMaxInflight(1000);
			options.setAutomaticReconnect(true);
			options.setWill(client.getTopic(userid + "/LWT"), "I'm gone :(".getBytes(), 0, false);
			client.connect(options);

			final MqttTopic Topic = client.getTopic(TOPIC);
			
			Topic.publish(new MqttMessage(JsonString.getBytes()));
			System.out.println("Published data. Topic: " + Topic.getName() + "   Message: Tag ID " + tagData.getTagid());

		} catch (MqttException e) {
			handleError(tagData);
		}
	}
	
	private static void handleError(TagData tagData) throws MqttException {
		//if error occurs less than 5 times try to publish again
		if (error<5) {
			System.out.println("\nError publishing message to MQTT Broker");
			System.out.println("Trying again...\n");
			error++;
			publish(tagData);
		}
		System.out.println("Message not published\n");
		error=0;
	}
}
