# Parking Node Arduino (UNO R4 WiFi)

This project reads a VL53L0X distance sensor and an inductive metal sensor (LJ18A3-8-Z/BY)
to determine whether a parking spot is occupied, and periodically sends the result
to a Spring Boot backend over WiFi.

## Features

- Clean C++ structure with headers (`include/`) and sources (`src/`)
- Adafruit VL53L0X library integration
- Inductive metal sensor on digital pin `METAL_PIN` (see `config.h`)
- Simple occupancy logic:
  - `distance < DISTANCE_THRESHOLD_MM` **AND** metal detected → `occupied = true`
- WiFi (UNO R4 WiFi, `WiFiS3` library)
- HTTP POST in JSON format to configurable backend endpoint
- Fail-safe:
  - If VL53L0X fails to initialize, the application halts instead of sending garbage
  - If measurement is invalid, it is not sent

## File structure

- `include/config.h` – all configuration (pins, thresholds, WiFi, backend, IDs)
- `include/sensors.h` – sensor API and `Measurement` struct
- `include/http_client.h` – WiFi / HTTP interface
- `src/sensors.cpp` – VL53L0X + metal sensor logic
- `src/http_client.cpp` – WiFi init and HTTP POST implementation
- `src/main.cpp` – `setup()` and `loop()`

## Configuration

Edit `include/config.h` before uploading:

```cpp
#define WIFI_SSID      "YOUR_WIFI_SSID"
#define WIFI_PASSWORD  "YOUR_WIFI_PASSWORD"

#define SERVER_HOST    "parkwise.local"   // IP or hostname of the ingest server
#define SERVER_PORT    5051
#define SERVER_PATH    "/ingest"
```

Pins, the 50 cm distance threshold and the measurement interval are also set there.
Do not commit real WiFi credentials.

## JSON payload

The Arduino sends a JSON body like:

```json
{
  "sensorId": "vlx-node-1",
  "spotId":   "P1",
  "distanceMm": 123,
  "metal": true,
  "occupied": false
}
```

On the server, readings were saved as JSON files that the data collector picks up over SFTP.

## Building with PlatformIO

1. Open this folder with VS Code + PlatformIO (or run `pio run` from the command line).
2. Run **Build**.
3. Connect your Arduino UNO R4 WiFi.
4. Run **Upload**.
5. Open **Monitor** at 115200 baud.

