package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SystemLogRepo extends JpaRepository<SystemLog, Long> {

    List<SystemLog> findTop100ByOrderByCreatedAtDesc();

    List<SystemLog> findByLogLevelOrderByCreatedAtDesc(String logLevel);

    List<SystemLog> findByLogCategoryOrderByCreatedAtDesc(String logCategory);

    List<SystemLog> findBySensorIdOrderByCreatedAtDesc(Long sensorId);

    @Query("SELECT l FROM SystemLog l WHERE l.createdAt >= :since ORDER BY l.createdAt DESC")
    List<SystemLog> findRecentLogs(@Param("since") Instant since);

    @Query("SELECT COUNT(l) FROM SystemLog l WHERE l.logLevel = :level")
    Long countByLogLevel(@Param("level") String level);

    @Query("SELECT COUNT(l) FROM SystemLog l WHERE l.createdAt >= :since AND l.logLevel = :level")
    Long countByLogLevelSince(@Param("level") String level, @Param("since") Instant since);
}
