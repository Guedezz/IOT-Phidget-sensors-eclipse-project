package servo.client;

import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.phidget22.PhidgetException;

import mqtt.utils.Utils;

public class SubscribeCallback implements MqttCallback {

	public static final String userid = "16022694"; // change this to be your student-id

	// Declare a default sensor object (no location, name/value set later)
	SensorData lock = new SensorData("unknown", "unknown", "unknown");
	// Declare GSON utility object
	Gson gson = new Gson();
	// Declare String to hold json representation of sensor object data
	String lockJson = new String();
	TagData oneTag = new TagData("unknown");

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("\n" + cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws PhidgetException, MqttException {
		System.out.println("\nCredentials received from MQTT broker.");
		// Send to server to be validated
		String responseFromServer = CredentialValidator.validateTag(message.toString());
		
		if (!responseFromServer.equals("Invalid credentials")) {
			oneTag = gson.fromJson(responseFromServer, TagData.class);
			openDoor(oneTag.getDoorid(), oneTag.getTagid(), oneTag.getTagvalue(), oneTag.getDoorname());
		}else {
			System.out.println("Not authorised to open this door");
			//Construct data object from message and publish to let the client know that the tag is not authorised
			oneTag = gson.fromJson(message.toString(), TagData.class);
			oneTag.setTagvalue("Not authorised");
			Publisher.publish(oneTag);
		}

		if ((userid + "/LWT").equals(topic)) {
			System.err.println("Sensor gone!");
		}
	}

	private void openDoor(int doorid, int tagid, String tagvalue, String doorname) throws PhidgetException, MqttException {
		System.out.println("\nOpening door\n");
		// PhidgetMotorMover.moveServoTo(180);

		// This publishes a real confirmation that the door has been opened to a different topic for the android App to pick up and update UI
		System.out.println("Publishing door status");
		//construct data object to be published
		oneTag.setDoorid(doorid);
		oneTag.setTagid(tagid);
		oneTag.setTagvalue(tagvalue);
		oneTag.setDoorname(doorname);
	
		Publisher.publish(oneTag);

		System.out.println("Saving operation to database");
		sendToServer(doorname);

		System.out.println("\nWaiting until motor at position 180");
		Utils.waitFor(3);
		System.out.println("Door open for 2 seconds");
		// PhidgetMotorMover.moveServoTo(0.0);
		Utils.waitFor(1);
		System.out.println("Motor back at position 0");
	}

	private void sendToServer(String doorname) {
		// Set Motor Json object and send to server to be stored in DB
		lock.setUserid(userid);
		lock.setSensorname("Motor");
		lock.setSensorvalue("Opened");
		lock.setAttempt("Success opening: " + doorname);
		lockJson = gson.toJson(lock);
		SensorToServer.sendToServer(lockJson);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// no-op
	}
}
