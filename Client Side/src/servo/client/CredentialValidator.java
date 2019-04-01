package servo.client;

import com.google.gson.Gson;
import com.phidget22.PhidgetException;

public class CredentialValidator {

	static String validateTag(String tagDataJsonString) throws PhidgetException {

		SensorData oneSensorData = new SensorData("Unknown", "unknown");
		TagData thisTag = new TagData("Unknown");
		TagData tagFromServer = new TagData("Unknown");
		Gson gson = new Gson();
		String thisTagJson;
		String returnString = new String();

		// convert string to TagData - for comparing with response from serverÏÏ
		thisTag = gson.fromJson(tagDataJsonString, TagData.class);
		// convert to Json String - for databse
		thisTagJson = gson.toJson(thisTag);
		// Send the tag name to server to be checked and save server response in String
		String response = SensorToServer.checkTagOnServer(thisTagJson);
		// Construct tag with data sent by server
		tagFromServer = gson.fromJson(response, TagData.class);

		// Data object to be sent to Database - do not include the attempt field yet as this will depend on server response
		oneSensorData.setSensorname("RFID");
		oneSensorData.setUserid(Subscriber.userid);
		oneSensorData.setSensorvalue(thisTag.getTagvalue());

		
		int thisTagID = thisTag.getTagid();
		int tagIDFromServer = tagFromServer.getTagid();
		int thisDoorID = thisTag.getDoorid();
		int doorIDFromServer = tagFromServer.getDoorid();
		// If tag from MQTT message matches response from server than tag is valid and can open motor
		if (thisTagID == tagIDFromServer && thisDoorID == doorIDFromServer) {
			thisTag.setDoorname(tagFromServer.getDoorname()); // get door name from database
			oneSensorData.setAttempt("Success opening: " + tagFromServer.getDoorname()); // Inform database of success
			returnString = gson.toJson(thisTag);
			System.out.println("Credentials Validated." + "[" + thisTagID + " " + thisDoorID + "]" + 
														  "["+ tagIDFromServer + " " + doorIDFromServer + "]");

		} else {
			returnString = "Invalid credentials";
			// inform database of fail tag attempt
			oneSensorData.setAttempt("Fail to open: " + thisTag.getDoorname());
			System.out.println("\nInvalid credentials! " + thisTag.getTagid()
								+ " does not have permission to open door ID: " + thisDoorID);
			System.out.println("Check if credentials are in database\n");
		}
		// Sent the data object to database
		thisTagJson = gson.toJson(oneSensorData);
		System.out.println("Saving operation to database");
		SensorToServer.sendToServer(thisTagJson);
		return returnString;
	}
}
