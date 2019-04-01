# IOT Phidget sensors eclipse project

This is an university project

This is an eclipse project written in java. It also includes an android project related to the same system

This project is divided into three parts:

Client side:
Will connect to IOT phidget sensors and read inputs from RFID cards or tags. Will publish a json object containing all the sensor information through an MQTT Broker.

Server side:
Will be subscribed to a MQTT topic and receive the JSON objects containing the RFID from clients. Upon receiving will connect to server, which will connect to a mySQL database to check if the credentials are accepted. If accepted it will send back an MQTT JSON Object with tag information and instruction to open a remote door (in the form of a phidget servo motor).

The App:
The Android app will connect to the same server above to check if user is in database and has authorisation to open the specific door.
