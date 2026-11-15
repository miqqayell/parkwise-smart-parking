#ifndef SENSORS_H
#define SENSORS_H

#include <Arduino.h>
#include "Adafruit_VL53L0X.h"
#include "config.h"

// Single measurement structure
struct Measurement {
    uint16_t distanceMm;   // Distance from VL53L0X
    bool     metalDetected;// TRUE if metal sensor detects car
    bool     occupied;     // TRUE if spot considered occupied
    bool     valid;        // TRUE if distance measurement is valid
};

// Initialize all sensors (VL53L0X + metal sensor pin)
bool sensorsInit();

// Read one combined measurement
Measurement readMeasurement();

#endif // SENSORS_H
