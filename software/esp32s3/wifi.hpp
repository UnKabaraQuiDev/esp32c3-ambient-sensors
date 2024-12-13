#ifndef wifi_hpp
#define wifi_hpp

#include <WiFi.h>
#include <MQTT.h>
#include <ESP32Ping.h>

#include "utils.hpp"

#define MQTT_EP_TEMP "sensors/temperature"
#define MQTT_EP_HUMI "sensors/humidity"
#define MQTT_EP_LIGH "sensors/light"

static WiFiClient net;
static MQTTClient client;

bool connectToWiFi();

bool connectToMQTT();

void publishDataFloat(const char* topic, const float msg);

#endif
