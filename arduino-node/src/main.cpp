#include <Arduino.h>
#include "sensors.h"
#include "config.h"
#include "http_client.h"

// Non-blocking timing
static uint32_t lastMeasurementMs = 0;

void setup() {
    Serial.begin(115200);
    // If using native USB board, you might want to wait for Serial:
    // while (!Serial) { ; }

    Serial.println(F("Parking node starting..."));

    // Initialize sensors
    if (!sensorsInit()) {
        Serial.println(F("ERROR: Failed to init sensors. Halting (fail-safe)."));
        // Fail-safe: stop here to avoid reporting random garbage.
        while (true) {
            delay(1000);
        }
    }
    Serial.println(F("Sensors initialized OK."));

    // Initialize WiFi (non-fatal if it fails; we still measure locally)
    if (!wifiInit()) {
        Serial.println(F("WARNING: WiFi init failed. Will keep trying in background."));
    }
}

void loop() {
    const uint32_t now = millis();

    if (now - lastMeasurementMs >= MEASUREMENT_INTERVAL_MS) {
        lastMeasurementMs = now;

        Measurement m = readMeasurement();

        if (!m.valid) {
            Serial.println(F("Measurement invalid or out of range."));
            return;
        }

        Serial.print(F("Distance: "));
        Serial.print(m.distanceMm);
        Serial.print(F(" mm | Metal: "));
        Serial.print(m.metalDetected ? F("YES") : F("NO"));
        Serial.print(F(" | OCCUPIED: "));
        Serial.println(m.occupied ? F("YES") : F("NO"));

        // Try to send to backend (non-fatal if fails)
        if (!sendMeasurement(m)) {
            Serial.println(F("Send failed (will try again later)."));
        }
    }

    // Other tasks could run here (interrupt handling, etc.)
}
