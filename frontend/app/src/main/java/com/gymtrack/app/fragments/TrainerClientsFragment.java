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

        adapter = new ClientAdapter(new ArrayList<>(), this::openClientDiary, this::openBiomarkers);
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
                            map.put("edad", obj.has("edad") && !obj.get("edad").isJsonNull() ? obj.get("edad").getAsInt() : 0);
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

    private void openClientDiary(Map<String, Object> client) {
        Object idObj = client.get("id");
        long clientId = idObj instanceof Number ? ((Number) idObj).longValue() : -1;
        if (clientId == -1) return;

        TrainingLogFragment fragment = new TrainingLogFragment();
        Bundle args = new Bundle();
        args.putLong("CLIENT_ID", clientId);
        fragment.setArguments(args);

        // Assumes the parent activity has a loadFragment method
        if (getActivity() instanceof com.gymtrack.app.TrainerHomeActivity) {
            ((com.gymtrack.app.TrainerHomeActivity) getActivity()).loadFragment(fragment);
        }
    }

    private void openBiomarkers(Map<String, Object> client) {
        Object idObj = client.get("id");
        long clientId = idObj instanceof Number ? ((Number) idObj).longValue() : -1;
        if (clientId == -1) return;

        android.content.Intent intent = new android.content.Intent(requireContext(),
                com.gymtrack.app.TrainerBiomarkersActivity.class);
        intent.putExtra("CLIENT_ID", clientId);
        intent.putExtra("CLIENT_NAME", (String) client.get("nombre"));
        startActivity(intent);
    }

    // ─── Interfaces ────────────────────────────────────────────────────────────

    interface OnClientClick {
        void onClick(Map<String, Object> client);
    }

    // ─── Adapter ───────────────────────────────────────────────────────────────

    private static class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

        private List<Map<String, Object>> data;
        private final OnClientClick diaryListener;
        private final OnClientClick biomarkerListener;

        ClientAdapter(List<Map<String, Object>> data,
                OnClientClick diaryListener, OnClientClick biomarkerListener) {
            this.data = data;
            this.diaryListener = diaryListener;
            this.biomarkerListener = biomarkerListener;
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
            h.tvEdad.setText(c.get("edad") + " años");
            h.tvLastWorkout.setText("—");

            h.btnDiary.setOnClickListener(v -> diaryListener.onClick(c));

            h.btnMetrics.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(),
                        com.gymtrack.app.TrainerClientMetricsActivity.class);
                Object idObj = c.get("id");
                long id = idObj instanceof Number ? ((Number) idObj).longValue() : -1L;
                intent.putExtra("CLIENT_ID", id);
                intent.putExtra("CLIENT_NAME", nombre);
                v.getContext().startActivity(intent);
            });

            h.btnBiomarkers.setOnClickListener(v -> biomarkerListener.onClick(c));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvAvatarLetter, tvRutina,
                    tvLastWorkout, tvPeso, tvAltura, tvEdad;
            Button btnDiary, btnMetrics, btnBiomarkers;

            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_client_name);
                tvEmail = v.findViewById(R.id.tv_client_email);
                tvAvatarLetter = v.findViewById(R.id.tv_avatar_letter);
                tvRutina = v.findViewById(R.id.tv_rutina);
                tvLastWorkout = v.findViewById(R.id.tv_last_workout);
                tvPeso = v.findViewById(R.id.tv_peso);
                tvAltura = v.findViewById(R.id.tv_altura);
                tvEdad = v.findViewById(R.id.tv_edad);
                btnDiary = v.findViewById(R.id.btn_diary);
                btnMetrics = v.findViewById(R.id.btn_metrics);
                btnBiomarkers = v.findViewById(R.id.btn_biomarkers);
            }
        }
    }
}
