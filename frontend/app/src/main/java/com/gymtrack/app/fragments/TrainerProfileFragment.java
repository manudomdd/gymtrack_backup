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

import com.gymtrack.app.R;

/**
 * Fragment del perfil del entrenador.
 * Equivale al _buildTrainerProfile() de TrainerHomeScreen.dart.
 *
 * Muestra información personal estática y estadísticas del entrenador.
 * TODO: Cargar datos reales del backend.
 */
public class TrainerProfileFragment extends Fragment {

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

        Button btnEdit = view.findViewById(R.id.btn_edit_profile);
        btnEdit.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil en desarrollo", Toast.LENGTH_SHORT).show());
    }
}
