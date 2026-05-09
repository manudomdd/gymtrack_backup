package com.gymtrack.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gymtrack.app.network.AuthRepository;

import java.util.Map;

public class TrainerClientMetricsActivity extends AppCompatActivity {

    private LinearLayout metricsContainer;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_metrics);

        authRepository = new AuthRepository(this);

        TextView tvName = findViewById(R.id.tv_client_name);
        metricsContainer = findViewById(R.id.ll_metrics_container);
        Button btnBack = findViewById(R.id.btn_back);

        long clientId = getIntent().getLongExtra("CLIENT_ID", -1);
        String clientName = getIntent().getStringExtra("CLIENT_NAME");

        if (clientName != null) {
            tvName.setText("Métricas de " + clientName);
        }

        btnBack.setOnClickListener(v -> finish());

        if (clientId != -1) {
            fetchMetrics(clientId);
        } else {
            Toast.makeText(this, "ID de cliente inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMetrics(long clientId) {
        authRepository.getClientProgress(clientId, new AuthRepository.ProgressCallback() {
            @Override
            public void onSuccess(Map<String, Double> metrics) {
                runOnUiThread(() -> {
                    metricsContainer.removeAllViews();
                    if (metrics.isEmpty()) {
                        addMetricView("Sin datos", 0.0);
                        return;
                    }
                    for (Map.Entry<String, Double> entry : metrics.entrySet()) {
                        addMetricView(entry.getKey(), entry.getValue());
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(TrainerClientMetricsActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addMetricView(String muscleGroup, double slope) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 16, 0, 16);

        TextView tvMuscle = new TextView(this);
        tvMuscle.setText(muscleGroup);
        tvMuscle.setTextColor(0xFFFFFFFF); // White
        tvMuscle.setTextSize(18);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvMuscle.setLayoutParams(p1);

        TextView tvSlope = new TextView(this);
        String evaluation = slope > 0 ? "Positivo" : (slope < 0 ? "Negativo" : "Estancado");
        tvSlope.setText(String.format("%.2f (%s)", slope, evaluation));
        tvSlope.setTextColor(slope > 0 ? 0xFF00FF00 : (slope < 0 ? 0xFFFF0000 : 0xFFFFA500)); // Green, Red, Orange
        tvSlope.setTextSize(18);

        row.addView(tvMuscle);
        row.addView(tvSlope);

        metricsContainer.addView(row);
    }
}
