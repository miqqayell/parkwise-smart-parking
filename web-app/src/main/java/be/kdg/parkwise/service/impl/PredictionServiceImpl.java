package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.dto.AiModelRequestDto;
import be.kdg.parkwise.dto.AiModelResponseDto;
import be.kdg.parkwise.service.PredictionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final RestTemplate restTemplate;

    @Value("${app.ai-model.url}")
    private String baseUrl;

    @Value("${app.ai-model.endpoint}")
    private String endpoint;

    public PredictionServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AiModelResponseDto fetchPredictions(LocalDateTime dateTime) {
        // Construct the full URL: http://localhost:8000/predict_all
        String fullUrl = baseUrl + endpoint;

        // 1. Extract only the HOUR (0-23) from the time
        int hour = dateTime.getHour();

        // 2. Create the specific payload: {"time_of_day": 14}
        AiModelRequestDto request = new AiModelRequestDto(hour);

        try {
            // 3. Send to FastAPI
            return restTemplate.postForObject(fullUrl, request, AiModelResponseDto.class);
        } catch (Exception e) {
            System.err.println("AI Model Connection Error: " + e.getMessage());
            return null; // The controller/frontend will handle the error
        }
    }
}