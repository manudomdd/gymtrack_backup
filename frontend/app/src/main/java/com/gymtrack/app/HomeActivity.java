package com.gymtrack.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.gymtrack.app.fragments.DashboardFragment;
import com.gymtrack.app.fragments.TrainingLogFragment;
import com.gymtrack.app.fragments.ClientProfileFragment;
import com.gymtrack.app.fragments.HealthFragment;
import com.gymtrack.app.network.AuthRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity principal para usuarios clientes.
 * Equivale a HomeScreen.dart de Flutter.
 *
 * Contiene un DrawerLayout con tres secciones:
 * - Dashboard (índice 0): estadísticas y accesos rápidos
 * - Registro de Entrenamiento (índice 1): calendario y lista de entrenamientos
 * - Métricas de Actividad (índice 2): pasos, sueño, calorías, etc.
 */
public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        authRepository = new AuthRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        // Toggle hamburger ↔ flecha
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_dashboard, R.string.nav_dashboard);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Selección del menú lateral
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
            } else if (id == R.id.nav_training) {
                loadFragment(new TrainingLogFragment());
            } else if (id == R.id.nav_profile) {
                loadFragment(new ClientProfileFragment());
            } else if (id == R.id.nav_health) {
                loadFragment(new HealthFragment());
            } else if (id == R.id.nav_logout) {
                showLogoutDialog();
            }
            return true;
        });

        // Fragmento inicial: Dashboard
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navView.setCheckedItem(R.id.nav_dashboard);
        }

        // Solicitar permisos y luego iniciar servicio de pasos
        checkPermissionsAndStartService();
    }

    private void checkPermissionsAndStartService() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.POST_NOTIFICATIONS
            };
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions = new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION};
        } else {
            startStepService();
            return;
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 100);
        } else {
            startStepService();
        }
    }

    private void startStepService() {
        Intent serviceIntent = new Intent(this, com.gymtrack.app.services.StepCounterService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // Incluso si no da todos los permisos, intentamos iniciar el servicio
            // El servicio ya tiene comprobaciones internas para el sensor
            startStepService();
        }
    }

    /** Carga un Fragment en el contenedor principal */
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** Muestra el diálogo de confirmación de cierre de sesión */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> {
                    authRepository.clearToken();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
