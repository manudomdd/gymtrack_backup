package com.gymtrack.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.gymtrack.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para el registro de entrenamientos.
 * Equivale a TrainingLogScreen de home_screen.dart.
 *
 * Incluye:
 * - Calendario manual con navegación por meses
 * - Lista de entrenamientos del día seleccionado (RecyclerView)
 * - Diálogo para añadir nuevos entrenamientos
 */
public class TrainingLogFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();
    private TextView tvMonthYear;
    private GridLayout gridDays;
    private RecyclerView rvWorkouts;
    private LinearLayout layoutEmpty;
    private WorkoutAdapter adapter;

    private final List<Map<String, Object>> workoutSessions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        gridDays = view.findViewById(R.id.grid_days);
        rvWorkouts = view.findViewById(R.id.rv_workouts);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        Button btnPrev = view.findViewById(R.id.btn_prev_month);
        Button btnNext = view.findViewById(R.id.btn_next_month);
        Button btnAdd = view.findViewById(R.id.btn_add_workout);

        // Datos de ejemplo igual que el Flutter
        addSampleData();

        adapter = new WorkoutAdapter(new ArrayList<>());
        rvWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWorkouts.setAdapter(adapter);

        btnPrev.setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        btnNext.setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, 1);
            refreshCalendar();
        });
        btnAdd.setOnClickListener(v -> showAddWorkoutDialog());

        refreshCalendar();
    }

    /** Añade los entrenamientos de ejemplo del proyecto Flutter */
    private void addSampleData() {
        Map<String, Object> w1 = new HashMap<>();
        w1.put("date", Calendar.getInstance());
        w1.put("exercise", "Press de Banca");
        w1.put("sets", 4);
        w1.put("reps", 8);
        w1.put("rir", 2);
        w1.put("comment", "Excelente sesión, muy fuerte hoy");
        workoutSessions.add(w1);

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Map<String, Object> w2 = new HashMap<>();
        w2.put("date", yesterday);
        w2.put("exercise", "Sentadilla");
        w2.put("sets", 3);
        w2.put("reps", 10);
        w2.put("rir", 1);
        w2.put("comment", "Un poco cansado pero completé todo");
        workoutSessions.add(w2);
    }

    /** Reconstruye el calendario para el mes/año de selectedDate */
    private void refreshCalendar() {
        // Cabecera mes/año
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String capitalised = fmt.format(selectedDate.getTime());
        capitalised = capitalised.substring(0, 1).toUpperCase() + capitalised.substring(1);
        tvMonthYear.setText(capitalised);

        gridDays.removeAllViews();
        int daysInMonth = selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;
            boolean isSelected = isSameDay(selectedDate, day);

            TextView tvDay = new TextView(requireContext());
            tvDay.setText(String.valueOf(day));
            tvDay.setTextColor(isSelected ? 0xFF0F0014 : 0xFFFFFFFF);
            tvDay.setTextSize(14);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDay.setPadding(4, 8, 4, 8);

            if (isSelected) {
                tvDay.setBackgroundResource(R.drawable.bg_avatar_magenta);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(2, 2, 2, 2);
            tvDay.setLayoutParams(params);

            tvDay.setOnClickListener(v -> {
                selectedDate.set(Calendar.DAY_OF_MONTH, d);
                refreshCalendar();
                refreshWorkoutList();
            });

            gridDays.addView(tvDay);
        }

        refreshWorkoutList();
    }

    private boolean isSameDay(Calendar cal, int day) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && day == today.get(Calendar.DAY_OF_MONTH)
                && cal.get(Calendar.DAY_OF_MONTH) == day;
    }

    /** Filtra y muestra los entrenamientos para el día seleccionado */
    private void refreshWorkoutList() {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> w : workoutSessions) {
            Calendar wDate = (Calendar) w.get("date");
            if (wDate != null
                    && wDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    && wDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
                    && wDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
                filtered.add(w);
            }
        }

        adapter.updateData(filtered);
        rvWorkouts.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Muestra el diálogo para añadir un nuevo entrenamiento */
    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_workout, null);

        TextInputEditText etExercise = dialogView.findViewById(R.id.et_exercise);
        TextInputEditText etSets = dialogView.findViewById(R.id.et_sets);
        TextInputEditText etReps = dialogView.findViewById(R.id.et_reps);
        TextInputEditText etRir = dialogView.findViewById(R.id.et_rir);
        TextInputEditText etComment = dialogView.findViewById(R.id.et_comment);

        new AlertDialog.Builder(requireContext())
                .setTitle("Registrar Entrenamiento")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String exercise = etExercise.getText() != null ? etExercise.getText().toString().trim() : "";
                    if (exercise.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Por favor ingresa el nombre del ejercicio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> newWorkout = new HashMap<>();
                    newWorkout.put("date", (Calendar) selectedDate.clone());
                    newWorkout.put("exercise", exercise);
                    newWorkout.put("sets", parseOrZero(etSets));
                    newWorkout.put("reps", parseOrZero(etReps));
                    newWorkout.put("rir", parseOrZero(etRir));
                    newWorkout.put("comment", etComment.getText() != null ? etComment.getText().toString() : "");
                    workoutSessions.add(newWorkout);
                    refreshWorkoutList();
                    Toast.makeText(requireContext(),
                            "Entrenamiento registrado correctamente", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private int parseOrZero(TextInputEditText et) {
        try {
            return et.getText() != null ? Integer.parseInt(et.getText().toString()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ─── Adapter interno ──────────────────────────────────────────────────────

    private static class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.VH> {

        private List<Map<String, Object>> data;

        WorkoutAdapter(List<Map<String, Object>> data) {
            this.data = data;
        }

        void updateData(List<Map<String, Object>> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_workout, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Map<String, Object> w = data.get(position);
            holder.tvExercise.setText((String) w.get("exercise"));
            holder.tvSets.setText(String.valueOf(w.get("sets")));
            holder.tvReps.setText(String.valueOf(w.get("reps")));
            holder.tvRir.setText(String.valueOf(w.get("rir")));

            String comment = (String) w.get("comment");
            if (comment != null && !comment.isEmpty()) {
                holder.tvComment.setVisibility(View.VISIBLE);
                holder.tvComment.setText("Comentario: " + comment);
            } else {
                holder.tvComment.setVisibility(View.GONE);
            }

            holder.btnEdit.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Editar en desarrollo", Toast.LENGTH_SHORT).show());
            holder.btnDelete.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Eliminar en desarrollo", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvExercise, tvSets, tvReps, tvRir, tvComment;
            View btnEdit, btnDelete;

            VH(@NonNull View itemView) {
                super(itemView);
                tvExercise = itemView.findViewById(R.id.tv_exercise_name);
                tvSets = itemView.findViewById(R.id.tv_sets);
                tvReps = itemView.findViewById(R.id.tv_reps);
                tvRir = itemView.findViewById(R.id.tv_rir);
                tvComment = itemView.findViewById(R.id.tv_comment);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}
