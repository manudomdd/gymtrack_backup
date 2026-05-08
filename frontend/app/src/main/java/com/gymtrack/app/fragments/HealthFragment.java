package com.gymtrack.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.gymtrack.app.R;
import com.gymtrack.app.network.AuthRepository;
import com.gymtrack.app.services.StepCounterService;

import java.io.IOException;

import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HealthFragment extends Fragment {

    private TextView tvSteps;
    private TextInputEditText etHours;
    private Spinner spinnerQuality;
    private Button btnSave;
    private SharedPreferences prefs;
    
    private final BroadcastReceiver stepsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = intent.getIntExtra("steps", 0);
            if (tvSteps != null) tvSteps.setText(String.valueOf(steps));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvSteps = view.findViewById(R.id.tv_steps_count);
        etHours = view.findViewById(R.id.et_sleep_hours);
        spinnerQuality = view.findViewById(R.id.spinner_sleep_quality);
        btnSave = view.findViewById(R.id.btn_save_sleep);
        
        prefs = requireContext().getSharedPreferences("gymtrack_prefs", Context.MODE_PRIVATE);
        tvSteps.setText(String.valueOf(prefs.getInt("daily_steps", 0)));

        // Configurar Spinner 1-10
        Integer[] ratings = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ratings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuality.setAdapter(adapter);
        spinnerQuality.setSelection(4); // Default 5

        btnSave.setOnClickListener(v -> saveSleepLog());
    }

    private void saveSleepLog() {
        String hoursStr = etHours.getText().toString();
        if (hoursStr.isEmpty()) return;

        double hours = Double.parseDouble(hoursStr);
        int quality = (int) spinnerQuality.getSelectedItem();

        JsonObject log = new JsonObject();
        log.addProperty("hours", hours);
        log.addProperty("quality", quality);
        
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date());
        log.addProperty("date", currentDate);

        saveSleepToBackend(log);
    }

    private void saveSleepToBackend(JsonObject log) {
        AuthRepository auth = new AuthRepository(requireContext());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();

        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(log.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/api/client/health/sleep")
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Sueño registrado correctamente", Toast.LENGTH_SHORT).show();
                            etHours.setText("");
                        } else {
                            Toast.makeText(getContext(), "Error al registrar sueño", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(stepsReceiver, new IntentFilter(StepCounterService.ACTION_STEPS_UPDATED), Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(stepsReceiver, new IntentFilter(StepCounterService.ACTION_STEPS_UPDATED));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(stepsReceiver);
    }
}
