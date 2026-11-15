package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.SystemLog;
import be.kdg.parkwise.repository.SystemLogRepo;
import be.kdg.parkwise.service.LoggingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoggingServiceImpl implements LoggingService {

    private final SystemLogRepo systemLogRepo;

    public LoggingServiceImpl(SystemLogRepo systemLogRepo) {
        this.systemLogRepo = systemLogRepo;
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String category, String message, Exception e) {
        logError(category, message, e, null, null);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String category, String message, Exception e, Long sensorId, Long spotId) {
        SystemLog log = new SystemLog("ERROR", category, message);
        log.setSensorId(sensorId);
        log.setSpotId(spotId);

        if (e != null) {
            log.setDetails(getStackTraceAsString(e));
        }

        captureSourceInfo(log);
        systemLogRepo.save(log);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWarn(String category, String message) {
        logWarn(category, message, null, null);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWarn(String category, String message, Long sensorId, Long spotId) {
        SystemLog log = new SystemLog("WARN", category, message);
        log.setSensorId(sensorId);
        log.setSpotId(spotId);
        captureSourceInfo(log);
        systemLogRepo.save(log);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInfo(String category, String message) {
        logInfo(category, message, null, null);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInfo(String category, String message, Long sensorId, Long spotId) {
        SystemLog log = new SystemLog("INFO", category, message);
        log.setSensorId(sensorId);
        log.setSpotId(spotId);
        captureSourceInfo(log);
        systemLogRepo.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemLog> getRecentLogs(int limit) {
        if (limit <= 0) {
            limit = 100;
        }
        return systemLogRepo.findTop100ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemLog> getLogsByLevel(String level) {
        return systemLogRepo.findByLogLevelOrderByCreatedAtDesc(level);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemLog> getLogsByCategory(String category) {
        return systemLogRepo.findByLogCategoryOrderByCreatedAtDesc(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getLogStatistics() {
        Map<String, Long> stats = new HashMap<>();

        Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);

        stats.put("totalErrors", systemLogRepo.countByLogLevel("ERROR"));
        stats.put("totalWarnings", systemLogRepo.countByLogLevel("WARN"));
        stats.put("totalInfo", systemLogRepo.countByLogLevel("INFO"));
        stats.put("errors24h", systemLogRepo.countByLogLevelSince("ERROR", last24Hours));
        stats.put("warnings24h", systemLogRepo.countByLogLevelSince("WARN", last24Hours));
        stats.put("totalLogs", systemLogRepo.count()); // Total count of all logs

        return stats;
    }

    // Helper method to capture source class and method
    private void captureSourceInfo(SystemLog log) {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // Find the first stack element that's not this class or Thread
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (!className.equals(LoggingServiceImpl.class.getName()) &&
                    !className.equals(Thread.class.getName()) &&
                    !className.startsWith("java.") &&
                    !className.startsWith("org.springframework.")) {

                    log.setSourceClass(getSimpleClassName(className));
                    log.setSourceMethod(element.getMethodName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore if we can't capture source info
        }
    }

    private String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
