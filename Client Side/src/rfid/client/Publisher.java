package rfid.client;

import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.phidget22.*;

public class Publisher {

	// public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
	public static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
	public static final String userid = "16022694"; // my student-id

	public static final String TOPIC = userid + "/openRequest";

	static TagData oneTag = new TagData("unknown");

	public static int doorid = 1; // this specific doorID is 1 - this could change for different doors as it is a
									// variable
	public static String tagvalue;
	public static int tagid = Integer.parseInt(userid);
	static String rfidJson = new String();

	static Gson gson = new Gson();

	private MqttClient client;

	RFID phid = new RFID();

	// @SuppressWarnings("unused")
	public static void main(String args[]) throws PhidgetException, MqttException {
		Publisher publisher = new Publisher();
		publisher.start();
	}

	public Publisher() throws PhidgetException {
		try {
			client = new MqttClient(BROKER_URL, userid);
			// create MQTT session
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setMaxInflight(1000);
			options.setAutomaticReconnect(true);
			options.setWill(client.getTopic(userid + "/LWT"), "I'm gone :(".getBytes(), 0, false);
			if (!client.isConnected()) {
				client.connect(options);
			}

		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		/***************** THIS IS TO BE USED WITH RFID BOARD - THIS CODE HAS NOT BEEN TESTED WITH THE RFID PHIDGET *************

		// Make the RFID Phidget able to detect loss or gain of an rfid card
		phid.addTagListener(new RFIDTagListener() {
			public void onTag(RFIDTagEvent e) {
				System.out.println("Tag in");
				oneTag.setTagid(tagid); // this could be the user id - however it had to be registered in the database
				oneTag.setTagvalue(e.getTag()); 
				oneTag.setDoorid(doorid); // this could be the rfid board - however it had to be registered in the database
				try {
					publishTag(oneTag);
				} catch (MqttException e1) {
					e1.printStackTrace();
				}
			}
		});

		phid.addTagLostListener(new RFIDTagLostListener() {
			public void onTagLost(RFIDTagLostEvent e) {
				System.out.println("Tag out");
			}
		});

		try {
			// Open and start detecting rfid cards
			phid.open(5000); // wait 5 seconds for device to respond

			// Display info on currently connected devices
			System.out.println("Device Name " + phid.getDeviceName());
			System.out.println("Serial Number " + phid.getDeviceSerialNumber());
			System.out.println("Device Version " + phid.getDeviceVersion());

			phid.setAntennaEnabled(true);

			System.out.println("\n\nGathering data\n\n");
			
		} catch (PhidgetException ex) {
			System.out.println(ex.getDescription());
			phid.close();
		}
		**************************************************************************************************/
	}

	
	
	
	
	/*****  The method below has been created to copy the beahaviour of an RFID Board *******
	 *****  In order to test the code above this will need to be commented out        ********/
	
	/*
	 * @throws PhidgetException
	 */
	void start() throws MqttException, PhidgetException {
		try {
			oneTag.setTagid(tagid);
			oneTag.setTagvalue("5c00c8debb"); // get this from getTag() method
			oneTag.setDoorid(doorid); // get this from actual rfid reader
			// Publish data every 10 seconds forever.
			while (true) {
				System.out.println("\nTag in!");
				Thread.sleep(500);
				System.out.println("Tag out!");
				// Publish TagData
				publishTag(oneTag);
				Thread.sleep(1000);
				System.out.println("Waiting 10 seconds for next tag read.");
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void publishTag(TagData tagData) throws MqttException {
		final MqttTopic Topic = client.getTopic(TOPIC);
		// Convert to string before sending
		rfidJson = gson.toJson(tagData);
		// publish Tag Data in string format
		Topic.publish(new MqttMessage(rfidJson.getBytes()));
		System.out.println("Tag data published. Topic: " + Topic.getName());
	}
}
