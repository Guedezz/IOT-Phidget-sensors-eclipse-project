
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * Servlet implementation class sensorToDB
 */

@WebServlet("/SensorServerDB")
public class SensorServerDB extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Gson gson = new Gson();

	Connection conn = null;
	Statement stmt;

	public void init(ServletConfig config) throws ServletException {
		// init method is run once at the start of the servlet loading
		// This will load the driver and establish a connection
		super.init(config);
		String user = "guedesc";
		String password = "greshvAl7";
		// Note none default port used, 6306 not 3306
		String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

		// Load the database driver
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}

		// get a connection with the user/pass
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Sensor to DB  server is up and running\n");
			// System.out.println("Upload sensor data with
			// http://localhost:8080/PhidgetServer/SensorServerDB?sensordata=some_sensor_data_in_json_format");
			// System.out.println("View last sensor reading at
			// http://localhost:8080/PhidgetServer/SensorServerDB?getdata=true\n\n");

			// System.out.println("DEBUG: Connection to database successful.");
			stmt = conn.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
			System.out.println("\nDid you alter the lines to set user/password in the sensor server code?");
		}
	} // init()

	public void destroy() {
		try {
			conn.close();
		} catch (SQLException se) {
			System.out.println(se);
		}
	} // destroy()

	public SensorServerDB() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setStatus(HttpServletResponse.SC_OK);
		// Declare a SensorData object to hold the incoming data
		SensorData oneSensor = new SensorData("unknown", "unknown");
		TagData oneTag = new TagData("unknown");
		// Check to see whether the client is requesting data or sending it
		String getdata = request.getParameter("getdata");
		String getRFID = request.getParameter("getRFID");
		try {
			init();
			// If there is not RFID data, check if there is any sensor data
			if (getRFID == null) {
				// if no getdata parameter, client is sending data
				if (getdata == null) {
					// getdata is null, therefore it is receiving data
					// Extract the parameter data holding the sensordata
					String sensorJsonString = request.getParameter("sensordata");

					// Problem if sensordata parameter not sent, or is invalid json
					if (sensorJsonString != null) {
						// Convert the json string to an object of type SensorData
						oneSensor = gson.fromJson(sensorJsonString, SensorData.class);
						// now update the table
						updateSensorTable(oneSensor);
					} // endif sensorJsonString not null
				} // end if getdata is null
				else { // else if getdata is not null
						// Update sensor values and send back response
					String userid = getdata;
					String resultsJson = retrieveSensorData(userid);
					PrintWriter out = response.getWriter();
					out.println(resultsJson);
					System.out.println("Response sent back to client\n");
					out.close();
				}

			} else // if there is RFID data coming from client
			{
				// Convert the json string to an object of type TagData
				oneTag = gson.fromJson(getRFID, TagData.class);

				int tagid = oneTag.getTagid();
				String responseJsonString = retrieveTagData(tagid);

				PrintWriter out = response.getWriter();
				// send back to client
				responseJsonString = responseJsonString.replace("[", "").replace("]", "");
				out.println(responseJsonString);
				System.out.println("Records sent back to client for tag ID: " + oneTag.getTagid() + "\n");
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Post is same as Get, so pass on parameters and do same
		doGet(request, response);
	}

	private void updateSensorTable(SensorData oneSensor) {
		try {
			// Create the INSERT statement from the parameters
			// set time inserted to be the current time on database server
			String updateSQL = "insert into "
					+ "sensorusage(userid, sensorname, sensorvalue, latitude, longitude, attempt, timeinserted) "
					+ "values('" + oneSensor.getUserid() + "','" + oneSensor.getSensorname() + "','"
					+ oneSensor.getSensorvalue() + "','" + oneSensor.getLatitude() + "','" + oneSensor.getLongitude()
					+ "','" + oneSensor.getAttempt() + "'," + "now());";

			System.out.println("DEBUG: Update: " + updateSQL);
			stmt.executeUpdate(updateSQL);
			System.out.println("DEBUG: Update successful ");
		} catch (SQLException se) {
			// Problem with update, return failure message
			System.out.println(se);
			System.out.println("\nDEBUG: Update error - see error trace above for help. ");
			return;
		}

		// all ok, return
		return;
	}

	private String retrieveSensorData(String userid) {
		String selectSQL = "select * from sensorusage where sensorvalue = 'opened' and userid='" + userid
				+ "' order by timeinserted desc limit 20;";
		ResultSet rs;

		// Declare ArrayList of sensors to hold results
		ArrayList<SensorData> allSensors = new ArrayList<SensorData>();
		try {
			// create a result set of selected values
			rs = stmt.executeQuery(selectSQL);

			// iterate over the result set
			while (rs.next()) {

				// Declare a SensorData object to hold individual values, initialise to defaults
				SensorData oneSensor = new SensorData("unknown", "unknown"); // fill in statement

				oneSensor.setSensorname(rs.getString("sensorname"));
				oneSensor.setSensorvalue(rs.getString("sensorvalue"));
				oneSensor.setUserid(rs.getString("userid"));
				oneSensor.setLatitude(rs.getDouble("latitude"));
				oneSensor.setLongitude(rs.getDouble("longitude"));
				oneSensor.setSensorname(rs.getString("sensorname"));
				oneSensor.setAttempt(rs.getString("attempt"));
				oneSensor.setSensordate(rs.getString("timeinserted"));

				// add this sensor to ArrayList of Sensors
				allSensors.add(oneSensor);
			}
		} catch (SQLException ex) {
			System.out.println("Error in SQL " + ex.getMessage());
		}
		System.out.println(allSensors);
		// Convert sensor list to json array and send back to user
		String allSensorsJson = gson.toJson(allSensors);
		System.out.println(allSensorsJson + "\n");
		// return this String from method
		return allSensorsJson;
	}

	private String retrieveTagData(int tagid) {
		String selectSQL = "select * from tags where tagid = '" + tagid + "';";
		ResultSet rs;

		// Declare ArrayList of sensors to hold results
		ArrayList<TagData> allTags = new ArrayList<TagData>();

		try {
			// create a result set of selected values
			rs = stmt.executeQuery(selectSQL);

			// Declare a SensorData object to hold individual values,
			// initialise to defaults
			TagData tagFromDatabase = new TagData("unknown"); // fill in statement
			// iterate over the result set
			while (rs.next()) {
				tagFromDatabase.setTagid(rs.getInt("tagid"));
				tagFromDatabase.setDoorid(rs.getInt("doorid"));
				tagFromDatabase.setTagvalue(rs.getString("tagvalue"));
				tagFromDatabase.setDoorname(rs.getString("doorname"));
				// add this sensor to ArrayList of Sensors
				allTags.add(tagFromDatabase);
				// debug print this sensor to console
			}
		} catch (SQLException ex) {
			System.out.println("Error in SQL " + ex.getMessage());
		}
		// Convert sensor list to json array and send back to user
		String allTagsJson = gson.toJson(allTags);

		// return this String from method
		return allTagsJson;
	}
}
