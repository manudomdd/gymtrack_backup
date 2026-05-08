package app.repository;

import app.entity.SleepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLog, Long> {
    List<SleepLog> findByUserId(Long userId);
    Optional<SleepLog> findByUserIdAndDate(Long userId, LocalDate date);
}
