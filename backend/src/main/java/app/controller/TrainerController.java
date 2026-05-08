package app.controller;

import app.entity.User;
import app.entity.WorkoutSession;
import app.repository.UserRepository;
import app.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    // Obtener los clientes de este entrenador
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getClients(Authentication auth) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        if (trainerOpt.isPresent()) {
            return ResponseEntity.ok(userRepo.findByTrainerId(trainerOpt.get().getId()));
        }
        return ResponseEntity.status(401).build();
    }

    // Asignar un cliente al entrenador
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

    // Obtener las métricas de progreso de un cliente
    @GetMapping("/client/{clientId}/progress")
    public ResponseEntity<Map<String, Double>> getClientProgress(Authentication auth, @PathVariable Long clientId) {
        // Validar si el cliente pertenece a este entrenador
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            if (clientOpt.get().getTrainer() != null
                    && clientOpt.get().getTrainer().getId().equals(trainerOpt.get().getId())) {
                return ResponseEntity.ok(workoutService.calculateProgressMetrics(clientId));
            }
        }
        return ResponseEntity.status(403).build();
    }

    // Asignar un entrenamiento a un cliente
    @PostMapping("/client/{clientId}/workouts")
    public ResponseEntity<WorkoutSession> assignWorkout(Authentication auth, @PathVariable Long clientId,
            @RequestBody WorkoutSession session) {
        Optional<User> trainerOpt = userRepo.findByEmail(auth.getName());
        Optional<User> clientOpt = userRepo.findById(clientId);

        if (trainerOpt.isPresent() && clientOpt.isPresent()) {
            if (clientOpt.get().getTrainer() != null
                    && clientOpt.get().getTrainer().getId().equals(trainerOpt.get().getId())) {
                session.setUser(clientOpt.get());
                return ResponseEntity.ok(workoutService.saveSession(session));
            }
        }
        return ResponseEntity.status(403).build();
    }
}
