package app.service;

import app.entity.WorkoutSession;
import app.repository.WorkoutSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutSessionRepository repository;

    public WorkoutSession saveSession(WorkoutSession session) {
        return repository.save(session);
    }

    /**
     * Persiste una lista completa de series de un mismo entrenamiento de una vez.
     * Cada elemento representa una serie individual con su número ordinal ya asignado.
     */
    public List<WorkoutSession> saveAllSessions(List<WorkoutSession> sessions) {
        return repository.saveAll(sessions);
    }

    public List<WorkoutSession> getSessionsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    /** Busca una sesión por ID para validación de permisos antes de actualizarla. */
    public Optional<WorkoutSession> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Calcula la regresión lineal (pendiente) del progreso en las cargas (peso)
     * para cada grupo muscular del usuario.
     *
     * Correcciones aplicadas:
     * - Usa la query optimizada del repositorio (ya ordenada por fecha ASC).
     * - Solo incluye grupos musculares con al menos 2 fechas distintas para
     *   garantizar que la pendiente calculada tiene significado real.
     * - Grupos con datos insuficientes se omiten del mapa (no devuelven 0.0
     *   falso que confundiría con "estancado").
     */
    public Map<String, Double> calculateProgressMetrics(Long userId) {
        List<WorkoutSession> allSessions = repository.findByUserId(userId);

        // Grupos musculares con peso registrado
        Set<String> muscleGroups = allSessions.stream()
                .filter(s -> s.getMuscleGroup() != null && s.getPeso() > 0)
                .map(WorkoutSession::getMuscleGroup)
                .collect(Collectors.toSet());

        Map<String, Double> metrics = new HashMap<>();

        for (String group : muscleGroups) {
            // Sesiones del grupo ordenadas por fecha (query de BD)
            List<WorkoutSession> sessions = repository
                    .findByUserIdAndMuscleGroupOrderByDateAsc(userId, group)
                    .stream()
                    .filter(s -> s.getPeso() > 0)
                    .collect(Collectors.toList());

            if (sessions.size() < 2) continue;

            // Se requieren al menos 2 fechas distintas para calcular una tendencia
            long distinctDates = sessions.stream()
                    .map(WorkoutSession::getDate)
                    .distinct()
                    .count();
            if (distinctDates < 2) continue;

            WorkoutSession first = sessions.get(0);
            int n = sessions.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

            for (WorkoutSession s : sessions) {
                double x = ChronoUnit.DAYS.between(first.getDate(), s.getDate());
                double y = s.getPeso();
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }

            double denominator = (n * sumX2) - (sumX * sumX);
            if (denominator == 0) continue; // Todos en la misma fecha (no debería ocurrir tras el check anterior)

            double slope = ((n * sumXY) - (sumX * sumY)) / denominator;
            metrics.put(group, slope);
        }

        return metrics;
    }
}
