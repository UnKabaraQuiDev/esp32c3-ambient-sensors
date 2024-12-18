#include "wifi.hpp"

// Wi-Fi credentials
const char* ssid = "INSEL";              // Replace with your Wi-Fi SSID
const char* password = "nootherperson";  // Replace with your Wi-Fi password
const IPAddress laptopIP = IPAddress(172, 17, 250, 139);

// MQTT broker credentials
const char* mqttServer = "172.17.250.139";  // Replace with your MQTT broker address
const int mqttPort = 1883;                  // Replace with your MQTT broker port, typically 1883
const char* mqttUser = NULL;                // Optional, MQTT username
const char* mqttPassword = NULL;            // Optional, MQTT password

bool connectToWiFi() {
  Serial.print("Connecting to Wi-Fi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("OK");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  {
    Serial.print(".");

    bool success = Ping.ping(laptopIP, 3);

    if (!success) {
      Serial.print("Server ping fail on: ");
      Serial.println(laptopIP.toString().c_str());
      return false;
    }

    Serial.print("Server ping succesful on: ");
    Serial.println(laptopIP.toString().c_str());
  }

  return true;
}

bool connectToMQTT() {
  Serial.print("Connecting to MQTT...");

  client.begin(mqttServer, net);

  while (!client.connect("arduino")) {
    Serial.print(".");
    delay(1000);
  }

  Serial.println("OK");

  return true;
}

void publishDataFloat(const char* topic, const float msg) {
  char result[8];

  if (client.publish(topic, toFloat(result, msg))) {
    Serial.println("Message published successfully");
  } else {
    Serial.println("Message publishing failed");
  }
}