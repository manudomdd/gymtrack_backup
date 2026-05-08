package app.repository;

import app.entity.StepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepLogRepository extends JpaRepository<StepLog, Long> {
    List<StepLog> findByUserId(Long userId);
    Optional<StepLog> findByUserIdAndDate(Long userId, LocalDate date);
}
