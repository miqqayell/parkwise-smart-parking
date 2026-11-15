#ifndef HTTP_CLIENT_H
#define HTTP_CLIENT_H

#include <Arduino.h>
#include "sensors.h"
#include "config.h"

// Initialize WiFi connection (blocking attempt during setup).
// Returns true if connected, false otherwise.
bool wifiInit();

// Try to send one measurement to the backend over HTTP.
// Returns true on (likely) success, false if connection or send failed.
bool sendMeasurement(const Measurement& m);

#endif // HTTP_CLIENT_H
