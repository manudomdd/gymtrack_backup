package com.gymtrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gymtrack.app.R;
import com.gymtrack.app.network.AuthRepository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fragment del perfil del entrenador.
 * Equivale al _buildTrainerProfile() de TrainerHomeScreen.dart.
 *
 * Muestra información personal estática y estadísticas del entrenador.
 * TODO: Cargar datos reales del backend.
 */
public class TrainerProfileFragment extends Fragment {

    private android.widget.TextView tvName, tvEmail, tvCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tv_trainer_name);
        tvEmail = view.findViewById(R.id.tv_trainer_email);
        tvCode = view.findViewById(R.id.tv_trainer_code);
        Button btnEdit = view.findViewById(R.id.btn_edit_profile);
        
        fetchTrainerProfile();

        btnEdit.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil en desarrollo", Toast.LENGTH_SHORT).show());
    }

    private void fetchTrainerProfile() {
        AuthRepository auth = new AuthRepository(requireContext());
        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/api/client/profile") // Same profile endpoint
                        .addHeader("Authorization", "Bearer " + auth.getToken())
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject obj = JsonParser.parseString(response.body().string()).getAsJsonObject();
                        
                        String nombre = obj.has("nombre") ? obj.get("nombre").getAsString() : "Sin nombre";
                        String email = obj.has("email") ? obj.get("email").getAsString() : "Sin email";
                        String code = obj.has("trainerCode") && !obj.get("trainerCode").isJsonNull() 
                                ? obj.get("trainerCode").getAsString() : "N/A";
                        
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            tvName.setText(nombre);
                            tvEmail.setText(email);
                            tvCode.setText(code);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
