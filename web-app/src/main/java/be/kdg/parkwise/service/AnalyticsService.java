package be.kdg.parkwise.service;

import java.util.Map;

public interface AnalyticsService {
    // Original analytics endpoints
    Map<String, Object> getOccupancyOverTime();
    Map<String, Object> getSpotHeatmap();
    Map<String, Object> getDistanceScatter();
    Map<String, Object> getPeakHours();
    Map<String, Object> getStats();

    // NEW: AI prediction endpoints
    Map<String, Object> getAIPredictions();
    Map<String, Object> getAIForecast();
    Map<String, Object> getAIBusynessChart();
}