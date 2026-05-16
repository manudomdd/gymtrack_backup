package com.gymtrack.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gymtrack.app.network.AuthRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TrainerBiomarkersActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private long clientId;

    private TextView tvMonthYear, tvSteps, tvSleepHours, tvSleepQuality, tvEmptyMessage;
    private Button btnPrevMonth, btnNextMonth, btnBack;
    private GridLayout gridDays;
    private LinearLayout layoutData, layoutEmpty;

    private Calendar currentCalendar;
    private Calendar selectedDate;

    private JsonArray sleepLogs = new JsonArray();
    private JsonArray stepLogs = new JsonArray();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_biomarkers);

        authRepository = new AuthRepository(this);

        clientId = getIntent().getLongExtra("CLIENT_ID", -1);
        String clientName = getIntent().getStringExtra("CLIENT_NAME");

        TextView tvName = findViewById(R.id.tv_client_name);
        if (clientName != null) {
            tvName.setText("Biomarcadores de " + clientName);
        }

        btnBack = findViewById(R.id.btn_back);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        tvMonthYear = findViewById(R.id.tv_month_year);
        gridDays = findViewById(R.id.grid_days);
        
        layoutData = findViewById(R.id.layout_data);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvSteps = findViewById(R.id.tv_steps);
        tvSleepHours = findViewById(R.id.tv_sleep_hours);
        tvSleepQuality = findViewById(R.id.tv_sleep_quality);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        currentCalendar = Calendar.getInstance();
        selectedDate = Calendar.getInstance();

        btnBack.setOnClickListener(v -> finish());
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        if (clientId != -1) {
            fetchHealthData();
        } else {
            Toast.makeText(this, "ID de cliente inválido", Toast.LENGTH_SHORT).show();
            finish();
        }

        refreshCalendar();
        updateUIForSelectedDate();
    }

    private void fetchHealthData() {
        authRepository.getClientHealth(clientId, new AuthRepository.HealthCallback() {
            @Override
            public void onSuccess(JsonObject health) {
                runOnUiThread(() -> {
                    if (health.has("sleepLogs")) {
                        sleepLogs = health.getAsJsonArray("sleepLogs");
                    }
                    if (health.has("stepLogs")) {
                        stepLogs = health.getAsJsonArray("stepLogs");
                    }
                    updateUIForSelectedDate();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(TrainerBiomarkersActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshCalendar() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String monthStr = meses[currentCalendar.get(Calendar.MONTH)] + " " + currentCalendar.get(Calendar.YEAR);
        tvMonthYear.setText(monthStr);

        gridDays.removeAllViews();

        Calendar calc = (Calendar) currentCalendar.clone();
        calc.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = calc.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int firstDayOfWeek = calc.get(Calendar.DAY_OF_WEEK);
        int emptyCells = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        for (int i = 0; i < emptyCells; i++) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setLayoutParams(new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ));
            gridDays.addView(tvEmpty);
        }

        for (int i = 1; i <= maxDays; i++) {
            TextView tvDay = new TextView(this);
            tvDay.setText(String.valueOf(i));
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setPadding(0, 16, 0, 16);
            tvDay.setTextSize(14);

            boolean isSelected = (currentCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    && currentCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
                    && i == selectedDate.get(Calendar.DAY_OF_MONTH));

            if (isSelected) {
                tvDay.setBackgroundResource(R.drawable.bg_badge_purple);
                tvDay.setTextColor(Color.WHITE);
                tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDay.setBackgroundColor(Color.TRANSPARENT);
                tvDay.setTextColor(Color.parseColor("#8AFFFFFF"));
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            tvDay.setLayoutParams(params);

            int finalI = i;
            tvDay.setOnClickListener(v -> {
                selectedDate.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), finalI);
                refreshCalendar();
                updateUIForSelectedDate();
            });

            gridDays.addView(tvDay);
        }
    }

    private void updateUIForSelectedDate() {
        String targetDate = dateFormat.format(selectedDate.getTime());
        
        boolean hasSteps = false;
        boolean hasSleep = false;

        // Reset data views
        tvSteps.setText("0");
        tvSleepHours.setText("0h");
        tvSleepQuality.setText("Calidad: 0/10");

        for (JsonElement el : stepLogs) {
            JsonObject log = el.getAsJsonObject();
            if (log.has("date") && log.get("date").getAsString().equals(targetDate)) {
                hasSteps = true;
                tvSteps.setText(String.valueOf(log.get("steps").getAsInt()));
                break;
            }
        }

        for (JsonElement el : sleepLogs) {
            JsonObject log = el.getAsJsonObject();
            if (log.has("date") && log.get("date").getAsString().equals(targetDate)) {
                hasSleep = true;
                tvSleepHours.setText(log.get("hoursSlept").getAsInt() + "h");
                tvSleepQuality.setText("Calidad: " + log.get("score").getAsInt() + "/10");
                break;
            }
        }

        if (hasSteps || hasSleep) {
            layoutData.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        } else {
            layoutData.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }
}
