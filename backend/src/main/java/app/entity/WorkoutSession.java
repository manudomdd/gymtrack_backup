package app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    private LocalDate date;
    private String exercise;
    private String muscleGroup; // "Pecho", "Espalda", etc.
    private int sets;
    private int reps;
    private int rir;
    private double peso; // Carga utilizada
    private String comment;
    private boolean completed;

    /** JSON serializado: [{"reps":8,"peso":80.0,"rir":2}, ...] — objetivo fijado por el entrenador. */
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String plannedSets;

    /** JSON serializado: [{"reps":7}, ...] — ejecución real registrada por el cliente. */
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String actualSets;

    public WorkoutSession() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getRir() {
        return rir;
    }

    public void setRir(int rir) {
        this.rir = rir;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPlannedSets() {
        return plannedSets;
    }

    public void setPlannedSets(String plannedSets) {
        this.plannedSets = plannedSets;
    }

    public String getActualSets() {
        return actualSets;
    }

    public void setActualSets(String actualSets) {
        this.actualSets = actualSets;
    }
}
