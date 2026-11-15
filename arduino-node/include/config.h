#ifndef CONFIG_H
#define CONFIG_H

#include <Arduino.h>

// --- Pin configuration ---
constexpr uint8_t METAL_PIN = 2;     // Inductive metal sensor signal (after resistor divider)

// --- Distance sensor configuration ---
constexpr uint16_t DISTANCE_THRESHOLD_MM = 500;      // 50 cm threshold for "something close"
constexpr uint16_t DISTANCE_OUT_OF_RANGE_MM = 9999;  // Value used when VL53L0X reading is invalid

// --- Measurement timing ---
constexpr uint32_t MEASUREMENT_INTERVAL_MS = 200;    // Approx. 5 Hz

// --- WiFi configuration (replace with your own) ---
#define WIFI_SSID      "YOUR_WIFI_SSID"
#define WIFI_PASSWORD  "YOUR_WIFI_PASSWORD"

// --- Backend (Spring Boot) configuration ---
#define SERVER_HOST    "parkwise.local"        // <-- replace with your server's IP or hostname
#define SERVER_PORT    5051
#define SERVER_PATH    "/ingest"

#endif // CONFIG_H
