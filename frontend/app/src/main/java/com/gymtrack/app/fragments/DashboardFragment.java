package com.gymtrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gymtrack.app.HomeActivity;
import com.gymtrack.app.R;

/**
 * Fragment del Dashboard principal del usuario cliente.
 * Equivale al _buildDashboard() de HomeScreen.dart.
 *
 * Muestra 4 tarjetas de estadísticas y botones de acceso rápido
 * para navegar a Registro de Entrenamiento y Métricas.
 */
public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNewTraining = view.findViewById(R.id.btn_new_training);
        Button btnSeeMetrics = view.findViewById(R.id.btn_see_metrics);

        // Navegar a Registro de Entrenamiento
        btnNewTraining.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).loadFragment(new TrainingLogFragment());
            }
        });

        // Navegar a Parámetros de Salud
        btnSeeMetrics.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).loadFragment(new HealthFragment());
            }
        });
    }
}
