package app.controller;

import app.entity.SleepLog;
import app.entity.StepLog;
import app.entity.User;
import app.entity.WorkoutSession;
import app.repository.UserRepository;
import app.service.HealthService;
import app.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private HealthService healthService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication auth) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        return userOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Metodo mediante el cual un cliente podrá actualizar los datos de su perfil.
     * @param auth
     * @param updateData
     * @return
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(Authentication auth, @RequestBody User updateData) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setNombre(updateData.getNombre());
            user.setEdad(updateData.getEdad());
            user.setPeso(updateData.getPeso());
            user.setAltura(updateData.getAltura());

            // Solo actualizamos NEAT si viene un valor distinto de cero (o si se desea mantener).
            if (updateData.getNeat() > 0) {
                user.setNeat(updateData.getNeat());
            }

            return ResponseEntity.ok(userRepo.save(user));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Enpoint el cual sirve para listar los entrenamientos. 
     * @param auth
     * @return
     */
    @GetMapping("/workouts")
    public ResponseEntity<List<WorkoutSession>> getWorkouts(Authentication auth) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(workoutService.getSessionsByUser(userOpt.get().getId()));
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Endpoint con metodo para añadir un nuevo ejercicio al entrenamiento.
     * @param auth
     * @param session
     * @return
     */
    @PostMapping("/workouts")
    public ResponseEntity<WorkoutSession> addWorkout(Authentication auth, @RequestBody WorkoutSession session) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            session.setUser(userOpt.get());
            return ResponseEntity.ok(workoutService.saveSession(session));
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Metodo para añadir un registro de sueño (numero de 1 a 10 calidad de sueño, con numero de horas dormidas).
     * @param auth
     * @param log
     * @return
     */
    @PostMapping("/health/sleep")
    public ResponseEntity<SleepLog> addSleepLog(Authentication auth, @RequestBody SleepLog log) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            log.setUser(userOpt.get());
            return ResponseEntity.ok(healthService.saveSleepLog(log));
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Metodo para añadir un nuevo registro de pasos. 
     * @param auth
     * @param log
     * @return
     */
    @PostMapping("/health/steps")
    public ResponseEntity<StepLog> addStepLog(Authentication auth, @RequestBody StepLog log) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            log.setUser(userOpt.get());
            return ResponseEntity.ok(healthService.saveStepLog(log));
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Metodo para enlazar un cliente con su correspondiente entrenador. 
     * @param auth
     * @param code
     * @return
     */
    @PostMapping("/link-trainer/{code}")
    public ResponseEntity<String> linkTrainer(Authentication auth, @PathVariable String code) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            User client = userOpt.get();
            Optional<User> trainerOpt = userRepo.findByTrainerCode(code);

            if (trainerOpt.isPresent() && trainerOpt.get().getTipoUsuario() == app.entity.TipoUsuario.ENTRENADOR) {
                client.setTrainer(trainerOpt.get());
                userRepo.save(client);
                return ResponseEntity.ok("Entrenador vinculado con éxito");
            } else {
                return ResponseEntity.badRequest().body("Código de entrenador inválido");
            }
        }
        return ResponseEntity.status(401).build();
    }
}
