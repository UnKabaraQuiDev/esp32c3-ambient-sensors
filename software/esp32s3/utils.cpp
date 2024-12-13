#ifndef utils_ino
#define utils_ino

char* toFloat(char* result, float f) {
  dtostrf(f, 6, 2, result);
  return result;
}

void halt() {
  Serial.print("Halting!");
  while (1) {
    delay(1000);
  }
}

#endif