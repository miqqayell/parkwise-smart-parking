#include "http_client.h"
#include <WiFiS3.h>

static WiFiClient httpClient;

// Internal helper to (re)connect WiFi if needed
static bool ensureWifiConnected() {
    if (WiFi.status() == WL_CONNECTED) {
        return true;
    }

    WiFi.disconnect();
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    // Try for some time, but don't block forever inside loop()
    const uint8_t maxAttempts = 10;
    for (uint8_t i = 0; i < maxAttempts; ++i) {
        if (WiFi.status() == WL_CONNECTED) {
            return true;
        }
        delay(500);
    }
    return (WiFi.status() == WL_CONNECTED);
}

bool wifiInit() {
    WiFi.disconnect();
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    Serial.print(F("Connecting to WiFi: "));
    Serial.println(F(WIFI_SSID));

    const uint8_t maxAttempts = 20;
    for (uint8_t i = 0; i < maxAttempts; ++i) {
        if (WiFi.status() == WL_CONNECTED) {
            Serial.println(F("WiFi connected."));
            Serial.print(F("IP Address: "));
            Serial.println(WiFi.localIP());
            return true;
        }
        delay(500);
        Serial.print(F("."));
    }

    Serial.println();
    Serial.println(F("WiFi connection FAILED."));
    return false;
}

bool sendMeasurement(const Measurement& m) {
    if (!m.valid) {
        // Don't send invalid data
        return false;
    }

    if (!ensureWifiConnected()) {
        Serial.println(F("WiFi not connected, cannot send measurement."));
        return false;
    }

    // Build small JSON body
    // Example:
    // {"sensorId":"vlx-node-1","spotId":"P1","distanceMm":123,"metal":true,"occupied":false}
    char body[160];
    const char* metalStr    = m.metalDetected ? "true" : "false";
    const char* occupiedStr = m.occupied      ? "true" : "false";

    // Use snprintf to avoid overflow
    int len = snprintf(
        body,
        sizeof(body),
        "{\"distanceMm\":%u,\"metal\":%s,\"occupied\":%s}",
        static_cast<unsigned>(m.distanceMm),
        metalStr,
        occupiedStr
    );

    if (len <= 0 || len >= static_cast<int>(sizeof(body))) {
        Serial.println(F("Error: JSON body too long."));
        return false;
    }

    Serial.println(F("Sending measurement to backend..."));
    Serial.print(F("JSON: "));
    Serial.println(body);

    if (!httpClient.connect(SERVER_HOST, SERVER_PORT)) {
        Serial.println(F("Connection to server failed."));
        return false;
    }

    // Build and send HTTP POST request
    httpClient.print(F("POST "));
    httpClient.print(F(SERVER_PATH));
    httpClient.println(F(" HTTP/1.1"));
    httpClient.print(F("Host: "));
    httpClient.print(F(SERVER_HOST));
    httpClient.print(F(":"));
    httpClient.println(SERVER_PORT);
    httpClient.println(F("Content-Type: application/json"));
    httpClient.print(F("Content-Length: "));
    httpClient.println(len);
    httpClient.println(F("Connection: close"));
    httpClient.println();
    httpClient.write(reinterpret_cast<const uint8_t*>(body), len);

    // Optional: read simple response (status line)
    unsigned long start = millis();
    while (httpClient.connected() && millis() - start < 2000UL) {
        if (httpClient.available()) {
            char c = httpClient.read();
            // You could parse status code here; for now we just print first line
            Serial.write(c);
        }
    }

    httpClient.stop();
    Serial.println();
    Serial.println(F("Measurement send attempt finished."));
    return true;
}
