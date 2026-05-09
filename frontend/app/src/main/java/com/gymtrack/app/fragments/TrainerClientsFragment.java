package com.gymtrack.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gymtrack.app.R;
import com.gymtrack.app.network.AuthRepository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Fragment para la lista de clientes del entrenador.
 * Equivale a TrainerClientsScreen de trainer_home_screen.dart.
 *
 * Incluye:
 * - Buscador en tiempo real por nombre o email
 * - RecyclerView con tarjeta por cliente
 * - Diálogo de detalles del cliente
 */
public class TrainerClientsFragment extends Fragment {

    private ClientAdapter adapter;
    private LinearLayout layoutEmpty;
    private RecyclerView rvClients;
    private final List<Map<String, Object>> allClients = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainer_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvClients = view.findViewById(R.id.rv_clients);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        TextInputEditText etSearch = view.findViewById(R.id.et_search);

        adapter = new ClientAdapter(new ArrayList<>(), this::showClientDetail);
        rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClients.setAdapter(adapter);

        fetchClients();

        // Búsqueda en tiempo real
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterClients(s.toString().toLowerCase().trim());
            }
        });

        updateVisibility(allClients);
    }

    private void fetchClients() {
        AuthRepository auth = new AuthRepository(requireContext());
        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/api/trainer/clients")
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
                        
                        allClients.clear();
                        for (JsonElement el : array) {
                            JsonObject obj = el.getAsJsonObject();
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", obj.get("id").getAsLong());
                            map.put("nombre", obj.get("nombre").getAsString());
                            map.put("email", obj.get("email").getAsString());
                            map.put("peso", obj.get("peso").getAsDouble());
                            map.put("altura", obj.get("altura").getAsInt());
                            map.put("rutina", "Plan Activo"); // Placeholder if not in User entity
                            map.put("estadoActual", "En forma"); // Placeholder
                            map.put("ultimoEntrenamiento", Calendar.getInstance()); // Placeholder
                            allClients.add(map);
                        }

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            adapter.updateData(new ArrayList<>(allClients));
                            updateVisibility(allClients);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void filterClients(String query) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> client : allClients) {
            String nombre = ((String) client.get("nombre")).toLowerCase();
            String email = ((String) client.get("email")).toLowerCase();
            if (nombre.contains(query) || email.contains(query)) {
                filtered.add(client);
            }
        }
        adapter.updateData(filtered);
        updateVisibility(filtered);
    }

    private void updateVisibility(List<Map<String, Object>> list) {
        boolean empty = list.isEmpty();
        rvClients.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    /** Muestra el diálogo para asignar una rutina a un cliente */
    private void showAssignRoutineDialog(Map<String, Object> clientData) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_workout, null);
        
        // Contenedor para Checkboxes de días
        LinearLayout daysContainer = new LinearLayout(requireContext());
        daysContainer.setOrientation(LinearLayout.VERTICAL);
        daysContainer.setPadding(20, 20, 20, 20);
        
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        android.widget.CheckBox[] checkBoxes = new android.widget.CheckBox[7];
        for (int i = 0; i < 7; i++) {
            checkBoxes[i] = new android.widget.CheckBox(requireContext());
            checkBoxes[i].setText(days[i]);
            checkBoxes[i].setTextColor(0xFFFFFFFF);
            daysContainer.addView(checkBoxes[i]);
        }
        
        ((LinearLayout) dialogView).addView(daysContainer, 0);

        TextInputEditText etExercise = dialogView.findViewById(R.id.et_exercise);
        android.widget.Spinner spinnerMuscleGroup = dialogView.findViewById(R.id.spinner_muscle_group);
        TextInputEditText etSets = dialogView.findViewById(R.id.et_sets);
        TextInputEditText etReps = dialogView.findViewById(R.id.et_reps);
        TextInputEditText etRir = dialogView.findViewById(R.id.et_rir);
        
        // Configurar Spinner de Grupos Musculares
        String[] groups = {"Pecho", "Espalda", "Hombro", "Bíceps", "Tríceps", "Cuádriceps", "Femorales"};
        android.widget.ArrayAdapter<String> groupAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(groupAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Asignar Rutina a " + clientData.get("nombre"))
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Asignar", (dialog, which) -> {
                    Object idObj = clientData.get("id");
                    long clientId = idObj instanceof Number ? ((Number) idObj).longValue() : -1;
                    if (clientId == -1) return;

                    String exercise = etExercise.getText() != null ? etExercise.getText().toString().trim() : "";
                    String setsStr = etSets.getText() != null ? etSets.getText().toString().trim() : "";
                    String repsStr = etReps.getText() != null ? etReps.getText().toString().trim() : "";
                    String rirStr = etRir.getText() != null ? etRir.getText().toString().trim() : "";

                    if (exercise.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty() || rirStr.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean daySelected = false;
                    for (int i = 0; i < 7; i++) {
                        if (checkBoxes[i].isChecked()) {
                            daySelected = true;
                            JsonObject workout = new JsonObject();
                            workout.addProperty("exercise", exercise);
                            workout.addProperty("muscleGroup", spinnerMuscleGroup.getSelectedItem().toString());
                            workout.addProperty("sets", Integer.parseInt(setsStr));
                            workout.addProperty("reps", Integer.parseInt(repsStr));
                            workout.addProperty("rir", Integer.parseInt(rirStr));
                            workout.addProperty("completed", false);
                            
                            Calendar targetDate = getNextDayOfWeek(i);
                            workout.addProperty("date", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(targetDate.getTime()));

                            assignWorkoutToClient(clientId, workout);
                        }
                    }
                    if (!daySelected) {
                        Toast.makeText(getContext(), "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private Calendar getNextDayOfWeek(int dayIndex) {
        // dayIndex: 0=Lunes, ..., 6=Domingo
        // Calendar.MONDAY = 2, ..., Calendar.SUNDAY = 1
        int targetCalDay = (dayIndex == 6) ? Calendar.SUNDAY : dayIndex + 2;
        
        Calendar cal = Calendar.getInstance();
        while (cal.get(Calendar.DAY_OF_WEEK) != targetCalDay) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal;
    }

    private void assignWorkoutToClient(long clientId, JsonObject workout) {
        AuthRepository auth = new AuthRepository(requireContext());
        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(workout.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/api/trainer/client/" + clientId + "/workouts")
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Entrenamiento asignado con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al asignar entrenamiento", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showClientDetail(Map<String, Object> client) {
        String nombre = (String) client.get("nombre");
        String email = (String) client.get("email");
        String rutina = (String) client.get("rutina");
        String estado = (String) client.get("estadoActual");
        Object peso = client.get("peso");
        Object altura = client.get("altura");
        Object id = client.get("id");

        String message = "Email: " + email
                + "\nPeso: " + peso + " kg"
                + "\nAltura: " + altura + " cm"
                + "\nRutina: " + rutina
                + "\nEstado: " + estado
                + "\nNº Cliente: " + id;

        new AlertDialog.Builder(requireContext())
                .setTitle(nombre)
                .setMessage(message)
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Editar",
                        (d, w) -> Toast.makeText(requireContext(), "Editar cliente en desarrollo", Toast.LENGTH_SHORT)
                                .show())
                .show();
    }

    private static Calendar daysAgo(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return c;
    }

    // ─── Adapter ──────────────────────────────────────────────────────────────

    interface OnClientClick {
        void onClick(Map<String, Object> client);
    }

    private static class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

        private List<Map<String, Object>> data;
        private final OnClientClick listener;

        ClientAdapter(List<Map<String, Object>> data, OnClientClick listener) {
            this.data = data;
            this.listener = listener;
        }

        void updateData(List<Map<String, Object>> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_client, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Map<String, Object> c = data.get(position);
            String nombre = (String) c.get("nombre");

            h.tvName.setText(nombre);
            h.tvEmail.setText((String) c.get("email"));
            h.tvAvatarLetter.setText(nombre.substring(0, 1).toUpperCase());
            h.tvRutina.setText((String) c.get("rutina"));
            h.tvPeso.setText(c.get("peso") + " kg");
            h.tvAltura.setText(c.get("altura") + " cm");
            h.tvEstado.setText((String) c.get("estadoActual"));

            // Último entrenamiento
            Calendar last = (Calendar) c.get("ultimoEntrenamiento");
            int daysAgo = (int) ((Calendar.getInstance().getTimeInMillis()
                    - last.getTimeInMillis()) / (1000 * 60 * 60 * 24));
            if (daysAgo == 0)
                h.tvLastWorkout.setText("Hoy");
            else if (daysAgo == 1)
                h.tvLastWorkout.setText("Ayer");
            else
                h.tvLastWorkout.setText("Hace " + daysAgo + " días");

            h.btnDetails.setOnClickListener(v -> listener.onClick(c));
            h.btnWorkouts.setText("Métricas");
            h.btnWorkouts.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.gymtrack.app.TrainerClientMetricsActivity.class);
                // Handle different numeric types gracefully
                Object idObj = c.get("id");
                long id = idObj instanceof Number ? ((Number) idObj).longValue() : 1L;
                intent.putExtra("CLIENT_ID", id);
                intent.putExtra("CLIENT_NAME", nombre);
                v.getContext().startActivity(intent);
            });
            h.btnPlan.setText("Asignar Rutina");
            h.btnPlan.setOnClickListener(v -> {
                if (listener instanceof TrainerClientsFragment) {
                    ((TrainerClientsFragment) listener).showAssignRoutineDialog(c);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvAvatarLetter, tvRutina, tvLastWorkout,
                    tvPeso, tvAltura, tvEstado;
            Button btnDetails, btnWorkouts, btnPlan;

            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_client_name);
                tvEmail = v.findViewById(R.id.tv_client_email);
                tvAvatarLetter = v.findViewById(R.id.tv_avatar_letter);
                tvRutina = v.findViewById(R.id.tv_rutina);
                tvLastWorkout = v.findViewById(R.id.tv_last_workout);
                tvPeso = v.findViewById(R.id.tv_peso);
                tvAltura = v.findViewById(R.id.tv_altura);
                tvEstado = v.findViewById(R.id.tv_estado);
                btnDetails = v.findViewById(R.id.btn_details);
                btnWorkouts = v.findViewById(R.id.btn_workouts);
                btnPlan = v.findViewById(R.id.btn_plan);
            }
        }
    }
}
