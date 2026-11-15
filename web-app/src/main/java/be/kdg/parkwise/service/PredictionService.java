package be.kdg.parkwise.service;

import be.kdg.parkwise.dto.AiModelResponseDto;
import java.time.LocalDateTime;

public interface PredictionService {
    AiModelResponseDto fetchPredictions(LocalDateTime dateTime);
}