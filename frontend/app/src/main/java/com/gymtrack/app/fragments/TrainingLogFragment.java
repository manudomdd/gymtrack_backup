package com.gymtrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gymtrack.app.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para el registro de entrenamientos.
 *
 * Flujo de registro (cliente):
 * 1. Pulsar "+ Añadir Entrenamiento".
 * 2. Introducir nombre del ejercicio y grupo muscular (cabecera común a todas las series).
 * 3. Pulsar "+ Añadir Serie" para añadir filas individuales (Peso, Reps, RIR, Comentario).
 * 4. Al guardar, se empaquetan todas las series en un JsonArray y se envían a
 *    POST /api/client/workouts/batch.
 *
 * Vista del entrenador: modo solo lectura (btnAdd oculto).
 * Lee de GET /api/trainer/client/{clientId}/workouts y muestra cada serie como card.
 */
public class TrainingLogFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();
    private TextView tvMonthYear;
    private GridLayout gridDays;
    private RecyclerView rvWorkouts;
    private LinearLayout layoutEmpty;
    private WorkoutAdapter adapter;

    // Lista completa de series recibidas del backend (modelo plano)
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

        // En modo entrenador (se recibe CLIENT_ID como argumento) ocultamos el botón de añadir
        long clientId = getArguments() != null ? getArguments().getLong("CLIENT_ID", -1) : -1;
        if (clientId != -1) {
            btnAdd.setVisibility(View.GONE);
        }

        adapter = new WorkoutAdapter(new ArrayList<>());
        rvWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWorkouts.setAdapter(adapter);

        btnPrev.setOnClickListener(v -> { selectedDate.add(Calendar.MONTH, -1); refreshCalendar(); });
        btnNext.setOnClickListener(v -> { selectedDate.add(Calendar.MONTH, 1); refreshCalendar(); });
        btnAdd.setOnClickListener(v -> showAddWorkoutDialog());

        refreshCalendar();
        fetchWorkoutsFromBackend();
    }

    // ─── Calendario ──────────────────────────────────────────────────────────

    /** Reconstruye el calendario para el mes/año de selectedDate */
    private void refreshCalendar() {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String cap = fmt.format(selectedDate.getTime());
        tvMonthYear.setText(cap.substring(0, 1).toUpperCase() + cap.substring(1));

        gridDays.removeAllViews();

        Calendar firstDayCal = (Calendar) selectedDate.clone();
        firstDayCal.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1, MONDAY = 2... ajustamos para que Lunes sea columna 0
        int emptySlots = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;

        for (int i = 0; i < emptySlots; i++) {
            TextView empty = new TextView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            empty.setLayoutParams(params);
            gridDays.addView(empty);
        }

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

            if (isSelected) tvDay.setBackgroundResource(R.drawable.bg_avatar_magenta);

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
        return cal.get(Calendar.DAY_OF_MONTH) == day;
    }

    // ─── Lista de Series ──────────────────────────────────────────────────────

    /** Filtra y muestra las series para el día seleccionado */
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

    // ─── Diálogo dinámico de añadir entrenamiento ─────────────────────────────

    /**
     * Diálogo con dos partes:
     *   - Cabecera fija: Nombre ejercicio + Grupo muscular
     *   - Contenedor dinámico: filas de series añadidas con "+ Añadir Serie"
     *
     * Cada fila contiene: Serie N (auto), Peso, Reps, RIR, Comentario.
     */
    private void showAddWorkoutDialog() {
        // Inflamos el diálogo en un ScrollView para poder hacer scroll cuando hay muchas series
        ScrollView scrollWrapper = new ScrollView(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_workout, null);
        scrollWrapper.addView(dialogView);

        TextInputEditText etExercise = dialogView.findViewById(R.id.et_exercise);
        android.widget.Spinner spinnerMuscleGroup = dialogView.findViewById(R.id.spinner_muscle_group);
        LinearLayout containerSeries = dialogView.findViewById(R.id.container_series);
        Button btnAddSeries = dialogView.findViewById(R.id.btn_add_series);

        // Spinner de grupos musculares
        String[] groups = {"Pecho", "Espalda", "Hombro", "Bíceps", "Tríceps", "Cuádriceps", "Femorales"};
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, groups);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(spinnerAdapter);

        // Lista que rastrea las vistas de cada serie añadida
        final List<View> seriesViews = new ArrayList<>();

        // Añadir la primera serie automáticamente para no dejar el diálogo vacío
        addSeriesRow(containerSeries, seriesViews);

        btnAddSeries.setOnClickListener(v -> addSeriesRow(containerSeries, seriesViews));

        new AlertDialog.Builder(requireContext())
                .setTitle("Registrar Entrenamiento")
                .setView(scrollWrapper)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String exercise = etExercise.getText() != null
                            ? etExercise.getText().toString().trim() : "";
                    if (exercise.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Introduce el nombre del ejercicio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (seriesViews.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Añade al menos una serie", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String muscleGroup = spinnerMuscleGroup.getSelectedItem().toString();
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(selectedDate.getTime());

                    // Construir el JsonArray con una entrada por serie
                    JsonArray seriesArray = new JsonArray();
                    for (int i = 0; i < seriesViews.size(); i++) {
                        View row = seriesViews.get(i);
                        TextInputEditText etPeso = row.findViewById(R.id.et_serie_peso);
                        TextInputEditText etReps = row.findViewById(R.id.et_serie_reps);
                        TextInputEditText etRir = row.findViewById(R.id.et_serie_rir);
                        TextInputEditText etComment = row.findViewById(R.id.et_serie_comment);

                        JsonObject serie = new JsonObject();
                        serie.addProperty("exercise", exercise);
                        serie.addProperty("muscleGroup", muscleGroup);
                        serie.addProperty("seriesNumber", i + 1);  // 1-indexed
                        serie.addProperty("peso", parseDoubleOrZero(etPeso));
                        serie.addProperty("reps", parseOrZero(etReps));
                        serie.addProperty("rir", parseOrZero(etRir));
                        serie.addProperty("comment",
                                etComment.getText() != null ? etComment.getText().toString() : "");
                        serie.addProperty("date", dateStr);
                        seriesArray.add(serie);
                    }

                    saveWorkoutBatchToBackend(seriesArray);
                })
                .show();
    }

    /**
     * Infla una nueva fila (item_series_row.xml), actualiza el número de serie
     * en la etiqueta y la añade al contenedor dinámico del diálogo.
     */
    private void addSeriesRow(LinearLayout container, List<View> seriesViews) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_series_row, container, false);
        int seriesNum = seriesViews.size() + 1;
        TextView tvLabel = row.findViewById(R.id.tv_series_label);
        tvLabel.setText("Serie " + seriesNum);
        container.addView(row);
        seriesViews.add(row);
    }

    // ─── Comunicación con el backend ─────────────────────────────────────────

    /**
     * POST /api/client/workouts/batch
     * Envía el JsonArray completo de series y refresca la lista al recibir 200.
     * Guard: si el token es null (sesión expirada), muestra error y aborta.
     */
    private void saveWorkoutBatchToBackend(JsonArray seriesArray) {
        com.gymtrack.app.network.AuthRepository auth =
                new com.gymtrack.app.network.AuthRepository(requireContext());

        String token = auth.getToken();
        if (token == null) {
            Toast.makeText(requireContext(),
                    "Sesión expirada. Por favor, vuelve a iniciar sesión.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        new Thread(() -> {
            try {
                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                        seriesArray.toString(),
                        okhttp3.MediaType.get("application/json; charset=utf-8"));
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://10.0.2.2:8080/api/client/workouts/batch")
                        .addHeader("Authorization", "Bearer " + token)
                        .post(body).build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
                            fetchWorkoutsFromBackend();
                        } else if (response.code() == 403 || response.code() == 401) {
                            Toast.makeText(getContext(),
                                    "Sesión expirada. Por favor, vuelve a iniciar sesión.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al guardar (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(
                        () -> Toast.makeText(getContext(),
                                "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * GET /api/client/workouts  (cliente)
     * GET /api/trainer/client/{id}/workouts  (entrenador en modo lectura)
     *
     * Parseo robusto: todos los campos opcionales se comprueban con isJsonNull()
     * para evitar NullPointerException si el cliente dejó algún campo vacío.
     */
    private void fetchWorkoutsFromBackend() {
        com.gymtrack.app.network.AuthRepository auth =
                new com.gymtrack.app.network.AuthRepository(requireContext());
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        new Thread(() -> {
            try {
                long clientId = getArguments() != null
                        ? getArguments().getLong("CLIENT_ID", -1) : -1;
                String url = clientId != -1
                        ? "http://10.0.2.2:8080/api/trainer/client/" + clientId + "/workouts"
                        : "http://10.0.2.2:8080/api/client/workouts";

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .get().build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JsonArray array =
                                com.google.gson.JsonParser.parseString(json).getAsJsonArray();

                        workoutSessions.clear();
                        for (JsonElement el : array) {
                            JsonObject obj = el.getAsJsonObject();
                            Map<String, Object> map = new HashMap<>();

                            // Campos obligatorios
                            map.put("id", safeGetLong(obj, "id", -1L));
                            map.put("exercise", safeGetString(obj, "exercise", "—"));
                            map.put("muscleGroup", safeGetString(obj, "muscleGroup", "—"));

                            // seriesNumber: número ordinal de la serie (campo "seriesNumber" en JSON)
                            map.put("seriesNumber", safeGetInt(obj, "seriesNumber", 1));

                            map.put("peso", safeGetDouble(obj, "peso", 0.0));
                            map.put("reps", safeGetInt(obj, "reps", 0));
                            map.put("rir", safeGetInt(obj, "rir", 0));

                            // Comentario puede ser null si el usuario lo dejó vacío
                            map.put("comment", safeGetString(obj, "comment", ""));

                            // Fecha
                            String dateStr = safeGetString(obj, "date", null);
                            if (dateStr != null) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        .parse(dateStr));
                                map.put("date", cal);
                            }

                            workoutSessions.add(map);
                        }

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(this::refreshWorkoutList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ─── Helpers de parseo robusto ───────────────────────────────────────────

    private String safeGetString(JsonObject obj, String key, String fallback) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        return obj.get(key).getAsString();
    }

    private int safeGetInt(JsonObject obj, String key, int fallback) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return fallback;
            return obj.get(key).getAsInt();
        } catch (Exception e) { return fallback; }
    }

    private double safeGetDouble(JsonObject obj, String key, double fallback) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return fallback;
            return obj.get(key).getAsDouble();
        } catch (Exception e) { return fallback; }
    }

    private long safeGetLong(JsonObject obj, String key, long fallback) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return fallback;
            return obj.get(key).getAsLong();
        } catch (Exception e) { return fallback; }
    }

    private int parseOrZero(TextInputEditText et) {
        try { return et.getText() != null ? Integer.parseInt(et.getText().toString()) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    private double parseDoubleOrZero(TextInputEditText et) {
        try { return et.getText() != null ? Double.parseDouble(et.getText().toString()) : 0.0; }
        catch (NumberFormatException e) { return 0.0; }
    }

    // ─── Adapter ──────────────────────────────────────────────────────────────

    /**
     * Adaptador para mostrar cada serie individual en el RecyclerView.
     * Funciona tanto para el cliente (modo escritura) como para el entrenador (modo lectura).
     */
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

            // Grupo muscular (subtítulo en mayúsculas)
            String muscleGroup = (String) w.get("muscleGroup");
            holder.tvMuscleGroup.setText(muscleGroup != null ? muscleGroup.toUpperCase() : "");

            // Nombre del ejercicio
            holder.tvExercise.setText((String) w.get("exercise"));

            // Número de serie ordinal
            Object seriesNum = w.get("seriesNumber");
            holder.tvSets.setText(seriesNum != null ? String.valueOf(seriesNum) : "—");

            // Peso
            Object peso = w.get("peso");
            holder.tvPeso.setText(peso != null ? String.valueOf(peso) : "—");

            // Repeticiones
            Object reps = w.get("reps");
            holder.tvReps.setText(reps != null ? String.valueOf(reps) : "—");

            // RIR
            Object rir = w.get("rir");
            holder.tvRir.setText(rir != null ? String.valueOf(rir) : "—");

            // Comentario: visible solo si no está vacío
            String comment = (String) w.get("comment");
            if (comment != null && !comment.trim().isEmpty()) {
                holder.tvComment.setVisibility(View.VISIBLE);
                holder.tvComment.setText("💬 " + comment);
            } else {
                holder.tvComment.setVisibility(View.GONE);
            }

            // Botón editar oculto; eliminar disponible para futura implementación
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(
                    v -> Toast.makeText(v.getContext(),
                            "Eliminar en desarrollo", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvMuscleGroup, tvExercise, tvSets, tvPeso, tvReps, tvRir, tvComment;
            View btnEdit, btnDelete;

            VH(@NonNull View itemView) {
                super(itemView);
                tvMuscleGroup = itemView.findViewById(R.id.tv_muscle_group);
                tvExercise = itemView.findViewById(R.id.tv_exercise_name);
                tvSets = itemView.findViewById(R.id.tv_sets);
                tvPeso = itemView.findViewById(R.id.tv_peso);
                tvReps = itemView.findViewById(R.id.tv_reps);
                tvRir = itemView.findViewById(R.id.tv_rir);
                tvComment = itemView.findViewById(R.id.tv_comment);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}
