package com.gymtrack.app.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
 *
 * Incluye:
 * - Buscador en tiempo real por nombre o email
 * - RecyclerView con tarjeta por cliente
 * - Diálogo de detalles del cliente
 * - Diálogo para asignar rutina con fecha exacta y series granulares por serie
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

        adapter = new ClientAdapter(new ArrayList<>(), this::showClientDetail, this::showAssignRoutineDialog);
        rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClients.setAdapter(adapter);

        fetchClients();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
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
                        .get().build();

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

    /**
     * Diálogo de asignación de rutina con:
     * - DatePickerDialog para fecha exacta (sincronización con el calendario del cliente)
     * - Entrada de número de series
     * - Botón "Configurar series" que genera filas dinámicas (reps / kg / rir por serie)
     * - Construye el JSON plannedSets y lo envía al endpoint del entrenador
     */
    private void showAssignRoutineDialog(Map<String, Object> clientData) {
        // Contenedor raíz con ScrollView para acomodar las series dinámicas
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);
        scrollView.addView(container);

        // ── Selección de fecha exacta ──────────────────────────────────────
        final String[] selectedDate = {
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime())
        };
        Button btnDate = new Button(requireContext());
        btnDate.setText("📅 Fecha: " + selectedDate[0]);
        container.addView(btnDate);

        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                selectedDate[0] = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                btnDate.setText("📅 Fecha: " + selectedDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── Nombre del ejercicio ───────────────────────────────────────────
        TextInputEditText etExercise = new TextInputEditText(requireContext());
        etExercise.setHint("Ejercicio (ej: Press banca)");
        container.addView(etExercise);

        // ── Grupo muscular ─────────────────────────────────────────────────
        Spinner spinnerMuscle = new Spinner(requireContext());
        String[] groups = {"Pecho", "Espalda", "Hombro", "Bíceps", "Tríceps", "Cuádriceps", "Femorales"};
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, groups);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscle.setAdapter(muscleAdapter);
        container.addView(spinnerMuscle);

        // ── Número de series ───────────────────────────────────────────────
        TextInputEditText etNumSets = new TextInputEditText(requireContext());
        etNumSets.setHint("Número de series");
        etNumSets.setInputType(InputType.TYPE_CLASS_NUMBER);
        container.addView(etNumSets);

        // ── Contenedor dinámico de filas por serie ─────────────────────────
        LinearLayout seriesContainer = new LinearLayout(requireContext());
        seriesContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(seriesContainer);

        // Listas para capturar los campos de cada serie
        List<TextInputEditText> repsFields = new ArrayList<>();
        List<TextInputEditText> pesoFields = new ArrayList<>();
        List<TextInputEditText> rirFields = new ArrayList<>();

        Button btnBuild = new Button(requireContext());
        btnBuild.setText("⚙ Configurar series");
        container.addView(btnBuild);

        btnBuild.setOnClickListener(v -> {
            String numStr = etNumSets.getText() != null ? etNumSets.getText().toString().trim() : "";
            if (numStr.isEmpty() || Integer.parseInt(numStr) <= 0) {
                Toast.makeText(getContext(), "Indica el número de series", Toast.LENGTH_SHORT).show();
                return;
            }
            int numSets = Integer.parseInt(numStr);
            seriesContainer.removeAllViews();
            repsFields.clear();
            pesoFields.clear();
            rirFields.clear();

            for (int i = 0; i < numSets; i++) {
                TextView label = new TextView(requireContext());
                label.setText("  Serie " + (i + 1));
                label.setTextColor(0xFFCCCCCC);
                seriesContainer.addView(label);

                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 4, 0, 4);

                TextInputEditText etReps = new TextInputEditText(requireContext());
                etReps.setHint("Reps");
                etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
                etReps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextInputEditText etPeso = new TextInputEditText(requireContext());
                etPeso.setHint("Kg");
                etPeso.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etPeso.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextInputEditText etRir = new TextInputEditText(requireContext());
                etRir.setHint("RIR");
                etRir.setInputType(InputType.TYPE_CLASS_NUMBER);
                etRir.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                row.addView(etReps);
                row.addView(etPeso);
                row.addView(etRir);
                seriesContainer.addView(row);

                repsFields.add(etReps);
                pesoFields.add(etPeso);
                rirFields.add(etRir);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Asignar Rutina a " + clientData.get("nombre"))
                .setView(scrollView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Asignar", (dialog, which) -> {
                    Object idObj = clientData.get("id");
                    long clientId = idObj instanceof Number ? ((Number) idObj).longValue() : -1;
                    if (clientId == -1) return;

                    String exercise = etExercise.getText() != null ? etExercise.getText().toString().trim() : "";
                    if (exercise.isEmpty()) {
                        Toast.makeText(getContext(), "Introduce el nombre del ejercicio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (repsFields.isEmpty()) {
                        Toast.makeText(getContext(), "Configura las series primero", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Construir plannedSets como JSON array
                    JsonArray plannedSets = new JsonArray();
                    double maxPeso = 0;
                    for (int i = 0; i < repsFields.size(); i++) {
                        String repsStr = repsFields.get(i).getText() != null ? repsFields.get(i).getText().toString().trim() : "0";
                        String pesoStr = pesoFields.get(i).getText() != null ? pesoFields.get(i).getText().toString().trim() : "0";
                        String rirStr = rirFields.get(i).getText() != null ? rirFields.get(i).getText().toString().trim() : "0";

                        int reps = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
                        double peso = pesoStr.isEmpty() ? 0.0 : Double.parseDouble(pesoStr);
                        int rir = rirStr.isEmpty() ? 0 : Integer.parseInt(rirStr);

                        if (peso > maxPeso) maxPeso = peso;

                        JsonObject serie = new JsonObject();
                        serie.addProperty("reps", reps);
                        serie.addProperty("peso", peso);
                        serie.addProperty("rir", rir);
                        plannedSets.add(serie);
                    }

                    JsonObject workout = new JsonObject();
                    workout.addProperty("exercise", exercise);
                    workout.addProperty("muscleGroup", spinnerMuscle.getSelectedItem().toString());
                    workout.addProperty("date", selectedDate[0]);
                    workout.addProperty("sets", repsFields.size());
                    // Peso máximo de las series como campo peso para métricas de progreso
                    workout.addProperty("peso", maxPeso);
                    workout.addProperty("reps", 0);   // Detalle en plannedSets
                    workout.addProperty("rir", 0);
                    workout.addProperty("completed", false);
                    workout.addProperty("plannedSets", plannedSets.toString());
                    workout.addProperty("actualSets", "[]");

                    assignWorkoutToClient(clientId, workout);
                })
                .show();
    }

    private void assignWorkoutToClient(long clientId, JsonObject workout) {
        AuthRepository auth = new AuthRepository(requireContext());
        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(workout.toString(),
                        MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/api/trainer/client/" + clientId + "/workouts")
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Rutina asignada con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al asignar: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(
                        () -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showClientDetail(Map<String, Object> client) {
        String nombre = (String) client.get("nombre");
        String message = "Email: " + client.get("email")
                + "\nPeso: " + client.get("peso") + " kg"
                + "\nAltura: " + client.get("altura") + " cm"
                + "\nNº Cliente: " + client.get("id");

        new AlertDialog.Builder(requireContext())
                .setTitle(nombre)
                .setMessage(message)
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Asignar Rutina", (d, w) -> showAssignRoutineDialog(client))
                .show();
    }

    // ─── Interfaces ────────────────────────────────────────────────────────────

    interface OnClientClick {
        void onClick(Map<String, Object> client);
    }

    // ─── Adapter ───────────────────────────────────────────────────────────────

    private static class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

        private List<Map<String, Object>> data;
        private final OnClientClick detailListener;
        private final OnClientClick assignListener;

        ClientAdapter(List<Map<String, Object>> data,
                OnClientClick detailListener, OnClientClick assignListener) {
            this.data = data;
            this.detailListener = detailListener;
            this.assignListener = assignListener;
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
            h.tvPeso.setText(c.get("peso") + " kg");
            h.tvAltura.setText(c.get("altura") + " cm");
            h.tvRutina.setText("—");
            h.tvEstado.setText("—");
            h.tvLastWorkout.setText("—");

            h.btnDetails.setOnClickListener(v -> detailListener.onClick(c));

            h.btnWorkouts.setText("Métricas");
            h.btnWorkouts.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(),
                        com.gymtrack.app.TrainerClientMetricsActivity.class);
                Object idObj = c.get("id");
                long id = idObj instanceof Number ? ((Number) idObj).longValue() : -1L;
                intent.putExtra("CLIENT_ID", id);
                intent.putExtra("CLIENT_NAME", nombre);
                v.getContext().startActivity(intent);
            });

            h.btnPlan.setText("Asignar Rutina");
            h.btnPlan.setOnClickListener(v -> assignListener.onClick(c));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvAvatarLetter, tvRutina,
                    tvLastWorkout, tvPeso, tvAltura, tvEstado;
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
