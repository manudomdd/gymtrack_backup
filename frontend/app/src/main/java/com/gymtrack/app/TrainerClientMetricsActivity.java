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

        // Contenedor de salud (creado programáticamente debajo de los de progreso)
        healthContainer = new LinearLayout(this);
        healthContainer.setOrientation(LinearLayout.VERTICAL);
        healthContainer.setPadding(0, 24, 0, 0);
        // Se añade al padre de metricsContainer para aparecer debajo
        ((LinearLayout) metricsContainer.getParent()).addView(healthContainer);

        if (clientId != -1) {
            fetchMetrics(clientId);
            fetchHealthData(clientId);
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

    // ── Datos de salud del cliente ─────────────────────────────────────────────

    private void fetchHealthData(long clientId) {
        authRepository.getClientHealth(clientId, new AuthRepository.HealthCallback() {
            @Override
            public void onSuccess(JsonObject health) {
                runOnUiThread(() -> renderHealthSection(health));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    addSectionHeader(healthContainer, "❤ Salud del cliente");
                    addInfoRow(healthContainer, "No disponible: " + message, 0xFF888888);
                });
            }
        });
    }

    private void renderHealthSection(JsonObject health) {
        healthContainer.removeAllViews();
        addSectionHeader(healthContainer, "❤ Salud del cliente");

        // ── Sueño ──────────────────────────────────────────────────────────
        addSectionHeader(healthContainer, "  😴 Sueño");
        JsonArray sleepLogs = health.has("sleepLogs") ? health.getAsJsonArray("sleepLogs") : new JsonArray();
        if (sleepLogs.size() == 0) {
            addInfoRow(healthContainer, "Sin registros de sueño", 0xFFAAAAAA);
        } else {
            for (JsonElement el : sleepLogs) {
                JsonObject log = el.getAsJsonObject();
                String date = log.has("date") ? log.get("date").getAsString() : "—";
                int hours = log.has("hoursSlept") ? log.get("hoursSlept").getAsInt() : 0;
                int score = log.has("score") ? log.get("score").getAsInt() : 0;
                addHealthRow(healthContainer, date, "🛏 " + hours + "h  |  Calidad: " + score + "/10",
                        score >= 7 ? 0xFF4CAF50 : score >= 4 ? 0xFFFFA726 : 0xFFE53935);
            }
        }

        // ── Pasos ──────────────────────────────────────────────────────────
        addSectionHeader(healthContainer, "  👟 Pasos diarios");
        JsonArray stepLogs = health.has("stepLogs") ? health.getAsJsonArray("stepLogs") : new JsonArray();
        if (stepLogs.size() == 0) {
            addInfoRow(healthContainer, "Sin registros de pasos", 0xFFAAAAAA);
        } else {
            for (JsonElement el : stepLogs) {
                JsonObject log = el.getAsJsonObject();
                String date = log.has("date") ? log.get("date").getAsString() : "—";
                int steps = log.has("steps") ? log.get("steps").getAsInt() : 0;
                int color = steps >= 10000 ? 0xFF4CAF50 : steps >= 6000 ? 0xFFFFA726 : 0xFFE53935;
                addHealthRow(healthContainer, date, steps + " pasos", color);
            }
        }
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

    private void addHealthRow(LinearLayout parent, String date, String value, int valueColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(8, 8, 0, 8);

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setTextColor(0xFF999999);
        tvDate.setTextSize(14);
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(valueColor);
        tvValue.setTextSize(14);

        row.addView(tvDate);
        row.addView(tvValue);
        parent.addView(row);
    }
}
