package be.kdg.parkwise.dto;

import java.util.Map;

public class AiModelResponseDto {
    private Map<String, Integer> predictions;

    public Map<String, Integer> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<String, Integer> predictions) {
        this.predictions = predictions;
    }
}