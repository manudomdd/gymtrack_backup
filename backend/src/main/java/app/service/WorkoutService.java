package app.service;

import app.entity.WorkoutSession;
import app.repository.WorkoutSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutSessionRepository repository;

    public WorkoutSession saveSession(WorkoutSession session) {
        return repository.save(session);
    }

    public List<WorkoutSession> getSessionsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Calcula la regresión lineal (pendiente) del progreso en las cargas 
     * (peso) para cada grupo muscular del usuario.
     */
    public Map<String, Double> calculateProgressMetrics(Long userId) {
        List<WorkoutSession> allSessions = repository.findByUserId(userId);
        
        // Agrupar por grupo muscular
        Map<String, List<WorkoutSession>> byMuscleGroup = allSessions.stream()
                .filter(s -> s.getMuscleGroup() != null && s.getPeso() > 0)
                .collect(Collectors.groupingBy(WorkoutSession::getMuscleGroup));

        Map<String, Double> metrics = new HashMap<>();

        for (Map.Entry<String, List<WorkoutSession>> entry : byMuscleGroup.entrySet()) {
            String muscleGroup = entry.getKey();
            List<WorkoutSession> sessions = entry.getValue();

            // Ordenar por fecha
            sessions.sort((s1, s2) -> s1.getDate().compareTo(s2.getDate()));

            if (sessions.size() < 2) {
                metrics.put(muscleGroup, 0.0);
                continue;
            }

            WorkoutSession firstSession = sessions.get(0);
            
            int n = sessions.size();
            double sumX = 0;
            double sumY = 0;
            double sumXY = 0;
            double sumX2 = 0;

            for (WorkoutSession s : sessions) {
                long daysDiff = ChronoUnit.DAYS.between(firstSession.getDate(), s.getDate());
                double x = (double) daysDiff;
                double y = s.getPeso();

                sumX += x;
                sumY += y;
                sumXY += (x * y);
                sumX2 += (x * x);
            }

            double denominator = (n * sumX2) - (sumX * sumX);
            double slope = 0.0;
            if (denominator != 0) {
                slope = ((n * sumXY) - (sumX * sumY)) / denominator;
            }
            
            metrics.put(muscleGroup, slope);
        }

        return metrics;
    }
}
