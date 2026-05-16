package com.gymtrack.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gymtrack.app.network.AuthRepository;

import java.util.Map;

/**
 * Pantalla de métricas de un cliente específico, accesible desde el perfil del entrenador.
 *
 * Muestra:
 * 1. Progreso de carga por grupo muscular (regresión lineal: pendiente + evaluación)
 * 2. Registros de salud del cliente (pasos diarios + sueño)
 */
public class TrainerClientMetricsActivity extends AppCompatActivity {

    private LinearLayout metricsContainer;
    private LinearLayout healthContainer;
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

    // ── Métricas de progreso de carga ──────────────────────────────────────────

    private void fetchMetrics(long clientId) {
        authRepository.getClientProgress(clientId, new AuthRepository.ProgressCallback() {
            @Override
            public void onSuccess(Map<String, Double> metrics) {
                runOnUiThread(() -> {
                    metricsContainer.removeAllViews();
                    addSectionHeader(metricsContainer, "📈 Progresión de carga por grupo muscular");
                    if (metrics.isEmpty()) {
                        addInfoRow(metricsContainer, "Sin datos suficientes aún", 0xFFAAAAAA);
                        return;
                    }
                    for (Map.Entry<String, Double> entry : metrics.entrySet()) {
                        addMetricView(entry.getKey(), entry.getValue());
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        TrainerClientMetricsActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addMetricView(String muscleGroup, double slope) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 12);

        TextView tvMuscle = new TextView(this);
        tvMuscle.setText(muscleGroup);
        tvMuscle.setTextColor(0xFFFFFFFF);
        tvMuscle.setTextSize(17);
        tvMuscle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvSlope = new TextView(this);
        String evaluation;
        int color;
        if (slope > 0.05) {
            evaluation = "↑ Progresando";
            color = 0xFF4CAF50;
        } else if (slope < -0.05) {
            evaluation = "↓ Retroceso";
            color = 0xFFE53935;
        } else {
            evaluation = "→ Estancado";
            color = 0xFFFFA726;
        }
        tvSlope.setText(String.format("%.2f kg/día  (%s)", slope, evaluation));
        tvSlope.setTextColor(color);
        tvSlope.setTextSize(15);

        row.addView(tvMuscle);
        row.addView(tvSlope);
        metricsContainer.addView(row);
    }

    // ── Helpers de UI ──────────────────────────────────────────────────────────

    private void addSectionHeader(LinearLayout parent, String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(18);
        tv.setPadding(0, 20, 0, 8);
        parent.addView(tv);
    }

    private void addInfoRow(LinearLayout parent, String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(15);
        tv.setPadding(8, 6, 0, 6);
        parent.addView(tv);
    }
}
