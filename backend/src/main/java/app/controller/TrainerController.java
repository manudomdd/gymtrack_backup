package app.controller;

import app.entity.User;
import app.entity.WorkoutSession;
import app.repository.UserRepository;
import app.service.HealthService;
import app.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private HealthService healthService;

    /**
     * Obtiene los clientes asignados a un entrenador determinado.
     */
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getClients(Authentication auth) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        if (trainerOpt.isPresent()) {
            return ResponseEntity.ok(userRepo.findByTrainerId(trainerOpt.get().getId()));
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Asigna un cliente a un entrenador.
     */
    @PostMapping("/assignClient/{clientId}")
    public ResponseEntity<String> assignClient(Authentication auth, @PathVariable Long clientId) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            User client = clientOpt.get();
            client.setTrainer(trainerOpt.get());
            userRepo.save(client);
            return ResponseEntity.ok("Cliente asignado exitosamente.");
        }
        return ResponseEntity.badRequest().body("Entrenador o Cliente no encontrados.");
    }

    /**
     * Obtiene las métricas de progreso (regresión lineal sobre peso) de un cliente.
     * El cliente debe estar vinculado al entrenador autenticado.
     */
    @GetMapping("/client/{clientId}/progress")
    public ResponseEntity<Map<String, Double>> getClientProgress(
            Authentication auth, @PathVariable Long clientId) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            User client = clientOpt.get();
            if (client.getTrainer() != null
                    && client.getTrainer().getId().equals(trainerOpt.get().getId())) {
                return ResponseEntity.ok(workoutService.calculateProgressMetrics(clientId));
            }
        }
        return ResponseEntity.status(403).build();
    }

    /**
     * Asigna una sesión de entrenamiento a un cliente del entrenador autenticado.
     * El cliente debe estar vinculado al entrenador; de lo contrario se devuelve 403.
     *
     * @param auth     autenticación del entrenador
     * @param clientId ID del cliente destinatario
     * @param session  datos de la sesión a asignar (incluye plannedSets)
     */
    @PostMapping("/client/{clientId}/workouts")
    public ResponseEntity<WorkoutSession> assignWorkout(Authentication auth,
            @PathVariable Long clientId, @RequestBody WorkoutSession session) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            User client = clientOpt.get();
            if (client.getTrainer() != null
                    && client.getTrainer().getId().equals(trainerOpt.get().getId())) {
                session.setUser(client);
                return ResponseEntity.ok(workoutService.saveSession(session));
            }
        }
        return ResponseEntity.status(403).build();
    }

    /**
     * Devuelve los registros de salud (sueño y pasos) de un cliente vinculado
     * al entrenador autenticado. Permite al entrenador cruzar los datos de bienestar
     * con el rendimiento en los entrenamientos.
     *
     * @param auth     autenticación del entrenador
     * @param clientId ID del cliente a consultar
     * @return mapa con "sleepLogs" y "stepLogs" del cliente
     */
    @GetMapping("/client/{clientId}/health")
    public ResponseEntity<Map<String, Object>> getClientHealth(
            Authentication auth, @PathVariable Long clientId) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            User client = clientOpt.get();
            if (client.getTrainer() != null
                    && client.getTrainer().getId().equals(trainerOpt.get().getId())) {
                Map<String, Object> health = new HashMap<>();
                health.put("sleepLogs", healthService.getSleepLogs(clientId));
                health.put("stepLogs", healthService.getStepLogs(clientId));
                return ResponseEntity.ok(health);
            }
        }
        return ResponseEntity.status(403).build();
    }
}
