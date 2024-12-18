#include "utils.hpp"

char* toFloat(char* result, float f) {
  snprintf(result, 8, "%8.2f", f);
  return result;
}

void halt() {
  Serial.print("Halting!");
  while (1) {
    delay(1000);
  }
}