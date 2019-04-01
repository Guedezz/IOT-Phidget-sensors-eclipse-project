package servo.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SensorToServer {

	public static String sensorServerURL = "http://localhost:8080/ServerSide/SensorServerDB";

	public static String sendToServer(String oneSensorJson) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		// Replace invalid URL characters from json string
		try {
			oneSensorJson = URLEncoder.encode(oneSensorJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String fullURL = sensorServerURL + "?sensordata=" + oneSensorJson;
		System.out.println("Sending data to: " + fullURL); // DEBUG confirmation message
		String line;
		String result = "";
		try {
			url = new URL(fullURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// Request response from server to enable URL to be opened
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//Send the RFID data to be checked on server
		public static String checkTagOnServer(String tagData) {
			URL url;
			HttpURLConnection conn;
			BufferedReader rd;
			// Replace invalid URL characters from json string
			try {
				tagData = URLEncoder.encode(tagData, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			String fullURL = sensorServerURL + "?getRFID=" + tagData;
			System.out.println("Validating credentials with Server..."); // DEBUG confirmation message
			String line;
			String result = "";
			try {
				url = new URL(fullURL);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				// Request response from server to enable URL to be opened
				while ((line = rd.readLine()) != null) {
					result += line;
				}
				rd.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
}
