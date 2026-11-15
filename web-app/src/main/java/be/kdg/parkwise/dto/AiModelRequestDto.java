package be.kdg.parkwise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiModelRequestDto {

    // Maps "timeOfDay" in Java to "time_of_day" in JSON for Python
    @JsonProperty("time_of_day")
    private int timeOfDay;

    public AiModelRequestDto(int timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public int getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(int timeOfDay) { this.timeOfDay = timeOfDay; }
}