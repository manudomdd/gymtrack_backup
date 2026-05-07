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
import com.gymtrack.app.TrainerHomeActivity;

/**
 * Fragment del Dashboard principal del entrenador.
 * Equivale al _buildDashboard() de TrainerHomeScreen.dart.
 */
public class TrainerDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSeeClients = view.findViewById(R.id.btn_see_clients);
        Button btnCreatePlan = view.findViewById(R.id.btn_create_plan);

        btnSeeClients.setOnClickListener(v -> {
            if (getActivity() instanceof TrainerHomeActivity) {
                ((TrainerHomeActivity) getActivity()).loadFragment(new TrainerClientsFragment());
            }
        });

        btnCreatePlan.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Crear plan en desarrollo", Toast.LENGTH_SHORT).show());
    }
}
