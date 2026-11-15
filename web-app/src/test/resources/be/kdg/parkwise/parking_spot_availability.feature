Feature: Real-time parking spot availability
  As a driver
  I want to see whether a parking spot is free or occupied right now
  So I don't waste time driving around looking for one

  Background:
    Given the parking database is clean

  Scenario: The API returns the current status based on the latest sensor reading
    Given a parking spot exists with label "Area A" and spot number 1
    And that spot has an active sensor
    And the latest sensor reading indicates the spot is FREE
    When the driver requests the parking spot statuses
    Then the response contains that spot with status "FREE"

  Scenario: The status changes when a newer sensor reading arrives
    Given a parking spot exists with label "Area A" and spot number 1
    And that spot has an active sensor
    And the latest sensor reading indicates the spot is FREE
    When the driver requests the parking spot statuses
    Then the response contains that spot with status "FREE"
    When a newer sensor reading indicates the spot is OCCUPIED
    And the driver requests the parking spot statuses
    Then the response contains that spot with status "OCCUPIED"

  Scenario: If a spot has no sensor, the status is UNKNOWN
    Given a parking spot exists with label "Area A" and spot number 1
    And that spot has no sensor
    When the driver requests the parking spot statuses
    Then the response contains that spot with status "UNKNOWN"

  Scenario: If a sensor has no readings, the status is UNKNOWN
    Given a parking spot exists with label "Area A" and spot number 1
    And that spot has an active sensor
    And the sensor has no readings
    When the driver requests the parking spot statuses
    Then the response contains that spot with status "UNKNOWN"
