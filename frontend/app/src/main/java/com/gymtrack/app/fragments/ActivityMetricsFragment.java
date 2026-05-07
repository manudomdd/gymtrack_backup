package com.gymtrack.app.fragments;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.gymtrack.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment de Métricas de Actividad.
 * Equivale a ActivityMetricsScreen de home_screen.dart.
 *
 * Muestra 5 métricas con barras de progreso:
 * Pasos, Sueño, Calorías, Frecuencia Cardíaca, Agua.
 */
public class ActivityMetricsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_metrics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cabecera con fecha actual
        TextView tvDate = view.findViewById(R.id.tv_date_header);
        SimpleDateFormat sdf = new SimpleDateFormat("'Hoy,' d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        tvDate.setText(sdf.format(new Date()));

        // Botón registrar métrica manual
        Button btnAdd = view.findViewById(R.id.btn_add_metric);
        btnAdd.setOnClickListener(v -> showAddMetricDialog());
    }

    /** Muestra el diálogo para registrar una métrica manual */
    private void showAddMetricDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_metric, null);

        Spinner spinner = dialogView.findViewById(R.id.spinner_metric);
        String[] options = {"Pasos", "Sueño (horas)", "Calorías", "Frecuencia Cardíaca (bpm)", "Agua (litros)"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        TextInputEditText etValue = dialogView.findViewById(R.id.et_value);

        new AlertDialog.Builder(requireContext())
                .setTitle("Registrar Métrica")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String value = etValue.getText() != null ? etValue.getText().toString().trim() : "";
                    if (value.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Por favor ingresa un valor", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(requireContext(),
                            "Métrica registrada correctamente", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
