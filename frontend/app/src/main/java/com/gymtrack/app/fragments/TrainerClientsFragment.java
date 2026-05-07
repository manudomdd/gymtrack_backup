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
import com.gymtrack.app.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<Map<String, Object>> allClients = buildSampleClients();

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

        adapter = new ClientAdapter(new ArrayList<>(allClients), this::showClientDetail);
        rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClients.setAdapter(adapter);

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

    /** Muestra el diálogo de detalles de un cliente */
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

    /** Lista de clientes de ejemplo, equivalente a la del Flutter */
    private static List<Map<String, Object>> buildSampleClients() {
        List<Map<String, Object>> list = new ArrayList<>();

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -1);

        Object[][] data = {
                { 1, "Juan García", "juan@gmail.com", 82.5, 178, "Bueno", cal1, "Push/Pull/Legs" },
                { 2, "Ana Martínez", "ana@gmail.com", 65.0, 168, "Muy Bueno", Calendar.getInstance(), "Full Body" },
                { 3, "Carlos López", "carlos@gmail.com", 90.0, 182, "Regular", daysAgo(3), "Upper/Lower" },
                { 4, "María Rodríguez", "maria@gmail.com", 70.5, 172, "Bueno", daysAgo(2), "HIIT" },
                { 5, "Pedro Sánchez", "pedro@gmail.com", 78.0, 175, "Muy Bueno", Calendar.getInstance(),
                        "Body Part Split" },
        };

        for (Object[] row : data) {
            Map<String, Object> c = new HashMap<>();
            c.put("id", row[0]);
            c.put("nombre", row[1]);
            c.put("email", row[2]);
            c.put("peso", row[3]);
            c.put("altura", row[4]);
            c.put("estadoActual", row[5]);
            c.put("ultimoEntrenamiento", row[6]);
            c.put("rutina", row[7]);
            list.add(c);
        }
        return list;
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
            h.btnWorkouts.setOnClickListener(
                    v -> Toast.makeText(v.getContext(), "Ver entrenamientos de " + nombre, Toast.LENGTH_SHORT).show());
            h.btnPlan.setOnClickListener(
                    v -> Toast.makeText(v.getContext(), "Editar plan de " + nombre, Toast.LENGTH_SHORT).show());
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
