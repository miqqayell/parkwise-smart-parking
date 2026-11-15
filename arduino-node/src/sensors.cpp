#include "sensors.h"

// VL53L0X global instance for this module only
static Adafruit_VL53L0X lox;

bool sensorsInit() {
    // Metal sensor input (external pull / divider, so plain INPUT)
    pinMode(METAL_PIN, INPUT);

    // Initialize VL53L0X
    if (!lox.begin()) {
        // Sensor did not start correctly
        return false;
    }

    return true;
}

Measurement readMeasurement() {
    Measurement m{};
    m.distanceMm    = DISTANCE_OUT_OF_RANGE_MM;
    m.metalDetected = false;
    m.occupied      = false;
    m.valid         = false;

    // ---- VL53L0X measurement ----
    VL53L0X_RangingMeasurementData_t measure;
    lox.rangingTest(&measure, false); // 'true' for debug output

    if (measure.RangeStatus == 4) {
        // Phase failure / out of range – keep valid=false
        return m;
    }

    m.distanceMm = measure.RangeMilliMeter;

    // ---- Metal sensor ----
    const int metalRaw = digitalRead(METAL_PIN);

    // Based on your previously working code:
    //   HIGH -> "not_car"
    //   LOW  -> "car"
    // So ACTIVE-LOW: LOW means metal/car is present.
    m.metalDetected = (metalRaw == HIGH);

    // ---- Logic ----
    const bool somethingClose = (m.distanceMm < DISTANCE_THRESHOLD_MM);
    m.occupied = (somethingClose && m.metalDetected);

    m.valid = true;
    return m;
}
