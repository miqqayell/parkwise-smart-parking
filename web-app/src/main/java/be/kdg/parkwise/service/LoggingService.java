package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.SystemLog;

import java.util.List;
import java.util.Map;

public interface LoggingService {

    void logError(String category, String message, Exception e);

    void logError(String category, String message, Exception e, Long sensorId, Long spotId);

    void logWarn(String category, String message);

    void logWarn(String category, String message, Long sensorId, Long spotId);

    void logInfo(String category, String message);

    void logInfo(String category, String message, Long sensorId, Long spotId);

    List<SystemLog> getRecentLogs(int limit);

    List<SystemLog> getLogsByLevel(String level);

    List<SystemLog> getLogsByCategory(String category);

    Map<String, Long> getLogStatistics();
}
