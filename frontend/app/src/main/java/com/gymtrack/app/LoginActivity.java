package com.gymtrack.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.gymtrack.app.network.AuthRepository;

/**
 * Pantalla de Login de GymTrack.
 * Equivale a LoginScreen.dart de Flutter.
 *
 * Permite al usuario iniciar sesión con email y contraseña.
 * En caso de éxito navega a HomeActivity.
 * Ofrece navegación a RegisterActivity para crear cuenta nueva.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnGoRegister;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoRegister = findViewById(R.id.btn_go_register);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    /** Gestiona el intento de login del usuario */
    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showSnackbar("Por favor, rellena todos los campos", false);
            return;
        }

        setLoading(true);

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSnackbar("¡Bienvenido!", true);
                    
                    // Redirigir según el rol
                    Class<?> targetActivity = HomeActivity.class;
                    if ("ENTRENADOR".equals(authRepository.getRole())) {
                        targetActivity = TrainerHomeActivity.class;
                    }

                    Intent intent = new Intent(LoginActivity.this, targetActivity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSnackbar("Error: Credenciales inválidas", false);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setVisibility(loading ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showSnackbar(String message, boolean success) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(success ? 0xFF00BF80 : 0xFFFF3333);
        snackbar.show();
    }
}
