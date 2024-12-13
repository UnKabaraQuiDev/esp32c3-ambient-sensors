#include "DHT.h"
#include "utils.hpp"
#include "wifi.hpp"

#define SENS_DHT_PIN 42
#define SENS_LIGHT_PIN 1
#define SENSOR_DATA_COUNT 10

#define DHTTYPE DHT11  // DHTTYPE = DHT11, but there are also DHT22 and 21

DHT dht(SENS_DHT_PIN, DHTTYPE);  // constructor to declare our sensor

void setup() {
  Serial.begin(115200);
  Serial.println("started");

  pinMode(SENS_LIGHT_PIN, INPUT);

  dht.begin();

  if (!connectToWiFi() || !connectToMQTT()) {
    halt();
  }
}

class SensorData;

bool readHumidity(SensorData *sensorData);
bool readTemperature(SensorData *sensorData);
bool readLightBrightness(SensorData *sensorData);
void computeAverage(SensorData* outputDatas, const SensorData* sensorDatas, const int count);

class SensorData {
private:
public:
  float humidity;
  float temperature;
  float lightBrightness;

  void dump(HardwareSerial &serial) {
    serial.printf("Temp: %.2f\n", this->temperature);
    serial.printf("Humi: %.2f\n", this->humidity);
    serial.printf("Ligh: %.2f\n", this->lightBrightness);
  }
};

bool readHumidity(SensorData *sensorData) {
  sensorData->humidity = dht.readHumidity();
  return !std::isnan(sensorData->humidity);
}

bool readTemperature(SensorData *sensorData) {
  sensorData->temperature = dht.readTemperature();
  return !std::isnan(sensorData->temperature);
}

bool readLightBrightness(SensorData *sensorData) {
  sensorData->lightBrightness = (float) (4096 - analogRead(SENS_LIGHT_PIN)) / 4096 * 100;
  return !std::isnan(sensorData->lightBrightness);
}

static SensorData persistent_sensor_datas[SENSOR_DATA_COUNT];
static uint8_t current_index = 0;

void computeAverage(SensorData* outputDatas, const SensorData* sensorDatas, const int count) {
  for(int i = 0; i < count; i++) {
    outputDatas->temperature += sensorDatas[i].temperature / count;
    outputDatas->humidity += sensorDatas[i].humidity / count;
    outputDatas->lightBrightness += sensorDatas[i].lightBrightness / count;
  }
}

void loop() {
  client.loop(); // handle mqtt transactions

  delay(250);

  SensorData* current_sensor_data = &persistent_sensor_datas[current_index];

  if (!readHumidity(current_sensor_data)) {
    Serial.println("Failed to read humidity");
  }

  if (!readTemperature(current_sensor_data)) {
    Serial.println("Failed to read temperature");
  }

  if (!readLightBrightness(current_sensor_data)) {
    Serial.println("Failed to read light brightness");
  }

  current_index++;
  if(current_index == SENSOR_DATA_COUNT) {
    SensorData average_sensor_data = {0, 0, 0};
    computeAverage(&average_sensor_data, persistent_sensor_datas, SENSOR_DATA_COUNT);

    publishDataFloat(MQTT_EP_TEMP, average_sensor_data.temperature);
    publishDataFloat(MQTT_EP_HUMI, average_sensor_data.humidity);
    publishDataFloat(MQTT_EP_LIGH, average_sensor_data.lightBrightness);

    Serial.println("Averages:");
    average_sensor_data.dump(Serial);
  }
  current_index %= SENSOR_DATA_COUNT;

  current_sensor_data->dump(Serial);
}
