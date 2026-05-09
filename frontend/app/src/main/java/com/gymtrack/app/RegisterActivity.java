package com.gymtrack.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.gymtrack.app.network.AuthRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla de Registro de GymTrack.
 * Equivale a RegisterScreen.dart de Flutter.
 *
 * Campos: nombre, email, password, peso, altura, NEAT (slider 1-5).
 * En caso de éxito vuelve al Login.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword, etPeso, etAltura;
    private Slider sliderNeat;
    private TextView tvNeatLabel;
    private Button btnRegister, btnGoLogin;
    private ProgressBar progressBar;
    private android.view.View tilTrainerCode;
    private TextInputEditText etTrainerCode;
    private AuthRepository authRepository;

    private int neatValue = 3;

    private static final String[] NEAT_LABELS = {
            "", "Muy Sedentario", "Sedentario", "Moderado", "Activo", "Muy Activo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);

        etNombre = findViewById(R.id.et_nombre);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPeso = findViewById(R.id.et_peso);
        etAltura = findViewById(R.id.et_altura);
        sliderNeat = findViewById(R.id.slider_neat);
        tvNeatLabel = findViewById(R.id.tv_neat_label);
        btnRegister = findViewById(R.id.btn_register);
        btnGoLogin = findViewById(R.id.btn_go_login);
        progressBar = findViewById(R.id.progress_bar);
        tilTrainerCode = findViewById(R.id.til_trainer_code);
        etTrainerCode = findViewById(R.id.et_trainer_code);

        // Lógica para mostrar/ocultar código de entrenador
        android.widget.RadioGroup rgRole = findViewById(R.id.rg_role);
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_entrenador) {
                tilTrainerCode.setVisibility(View.GONE);
            } else {
                tilTrainerCode.setVisibility(View.VISIBLE);
            }
        });

        // Listener del slider NEAT
        sliderNeat.addOnChangeListener((slider, value, fromUser) -> {
            neatValue = (int) value;
            tvNeatLabel.setText("NIVEL DE ACTIVIDAD FISICA DIARIA: " + NEAT_LABELS[neatValue]);
        });

        btnRegister.setOnClickListener(v -> handleRegister());
        btnGoLogin.setOnClickListener(v -> finish());
    }

    /** Gestiona el registro del nuevo usuario */
    private void handleRegister() {
        String nombre = getText(etNombre);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String pesoStr = getText(etPeso);
        String alturaStr = getText(etAltura);

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()
                || pesoStr.isEmpty() || alturaStr.isEmpty()) {
            showSnackbar("Por favor, rellena todos los campos", false);
            return;
        }

        double peso;
        int altura;
        try {
            peso = Double.parseDouble(pesoStr);
            altura = Integer.parseInt(alturaStr);
        } catch (NumberFormatException e) {
            showSnackbar("Peso y altura deben ser números válidos", false);
            return;
        }

        setLoading(true);

        String tipoUsuario = "CLIENTE";
        android.widget.RadioGroup rgRole = findViewById(R.id.rg_role);
        if (rgRole.getCheckedRadioButtonId() == R.id.rb_entrenador) {
            tipoUsuario = "ENTRENADOR";
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("email", email);
        userData.put("password", password);
        userData.put("peso", peso);
        userData.put("altura", altura);
        userData.put("neat", neatValue);
        userData.put("tipoUsuario", tipoUsuario);
        
        if (tipoUsuario.equals("CLIENTE")) {
            String trainerCode = etTrainerCode.getText().toString().trim();
            if (!trainerCode.isEmpty()) {
                userData.put("trainerCode", trainerCode);
            }
        }

        authRepository.register(userData, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSnackbar("¡Cuenta creada con éxito! Inicia sesión.", true);
                    // Volver al Login tras un breve delay
                    btnRegister.postDelayed(() -> finish(), 1500);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSnackbar("Error: El email ya existe o hay un problema", false);
                });
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnRegister.setVisibility(loading ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showSnackbar(String message, boolean success) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(success ? 0xFF00BF80 : 0xFFFF3333);
        snackbar.show();
    }
}
