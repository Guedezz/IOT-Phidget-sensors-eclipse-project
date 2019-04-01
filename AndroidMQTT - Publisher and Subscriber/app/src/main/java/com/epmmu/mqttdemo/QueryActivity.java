package com.epmmu.mqttdemo;


import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class QueryActivity extends AppCompatActivity {

    String[] record;
    Button backBtn;
    ArrayList<SensorData> allSensors = new ArrayList<>();
    String userid = "16022694";
    ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        myListView = (ListView) findViewById(R.id.myListView);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        GetData getData = new GetData();
        getData.execute();
    }

    //Asynk Task does all network call in the background
    private class GetData extends AsyncTask<Void, Void, ArrayList<SensorData>> {

        @Override
        protected ArrayList<SensorData> doInBackground(Void... voids) {
            getRecords(userid);
            return allSensors;
        }

        @Override
        protected void onPostExecute(ArrayList<SensorData> sensorDataArrayList) {
            super.onPostExecute(sensorDataArrayList);
            setArrayAdapter();
        }
    }


    private String getRecords(String userid) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String sensorServerURL = "http://10.0.2.2:8080/ServerSide/SensorServerDB";

        // Replace invalid URL characters from json string
        try {
            userid = URLEncoder.encode(userid, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String fullURL = sensorServerURL + "?getdata=" + userid;
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
            conn.disconnect();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        // print the response to log cat
        System.out.println("Server response = " + result);

        try {
            // declare a new json array and pass it the string response from the server
            // this will convert the string into a JSON array which we can then iterate over using a loop
            JSONArray jsonArray = new JSONArray(result);

            // instantiate the array and set the size to the amount of sensor data object returned by the server
            record = new String[jsonArray.length()];
            // use a for loop to iterate over the JSON array
            for (int i = 0; i < jsonArray.length(); i++) {
                // the following line of code will get the student details from the current JSON object and store it in a string variable
                String useridFromServer = jsonArray.getJSONObject(i).get("userid").toString();
                String sensorname = jsonArray.getJSONObject(i).get("sensorname").toString();
                String sensorvalue = jsonArray.getJSONObject(i).get("sensorvalue").toString();
                String latitude = jsonArray.getJSONObject(i).get("latitude").toString();
                String longitude = jsonArray.getJSONObject(i).get("longitude").toString();
                String sensordate = jsonArray.getJSONObject(i).get("sensordate").toString();
                String attempt = jsonArray.getJSONObject(i).get("attempt").toString();

                //Create a new sensor object and fill it in with values received from server
                SensorData sensorDataObject = new SensorData(sensorname, sensorvalue,
                                                            userid, Double.parseDouble(longitude),
                                                            Double.parseDouble(latitude),
                                                            sensordate, attempt);
                //Add to the arrayList
                allSensors.add(sensorDataObject);

                // print the name to log cat
                System.out.println("userid = " + useridFromServer);
                System.out.println("date = " + sensordate);
                // add the details of the current sensor to the record array
                record[i] = "User: " + useridFromServer + "\n" + attempt + "\nDate and Time: " + sensordate;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void setArrayAdapter() {
        // an array adapter to do all the hard work just tell it the (context, the layout, and the data)
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, record);
        //set the adapter to the listview
        myListView.setAdapter(arrayAdapter);
    }
}
