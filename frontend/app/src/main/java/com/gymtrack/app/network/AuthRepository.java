package com.gymtrack.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Repositorio de autenticación que maneja las llamadas HTTP al backend.
 * Equivale al AuthService de Flutter.
 * Endpoint base: http://10.0.2.2:8080/api/auth
 * (10.0.2.2 es la IP que el emulador Android usa para acceder al host)
 */
public class AuthRepository {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/auth";
    private static final String PREFS_NAME = "gymtrack_prefs";
    private static final String KEY_TOKEN = "token";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Interfaz de callback para operaciones asíncronas */
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Realiza el login con email y contraseña.
     * Si tiene éxito, guarda el JWT en SharedPreferences.
     * Se ejecuta en un hilo separado; el callback corre en el hilo llamante.
     */
    public void login(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("email", email);
                body.addProperty("password", password);

                RequestBody requestBody = RequestBody.create(body.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/login")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        if (jsonResponse.has("token")) {
                            String token = jsonResponse.get("token").getAsString();
                            prefs.edit().putString(KEY_TOKEN, token).apply();
                        }
                        callback.onSuccess();
                    } else {
                        callback.onError("Credenciales inválidas");
                    }
                }
            } catch (IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Registra un nuevo usuario con los datos proporcionados.
     * userData debe contener: nombre, email, password, peso, altura, neat, tipoUsuario
     */
    public void register(Map<String, Object> userData, AuthCallback callback) {
        new Thread(() -> {
            try {
                String bodyJson = gson.toJson(userData);
                RequestBody requestBody = RequestBody.create(bodyJson, JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/register")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Error en el registro. El email puede que ya exista.");
                    }
                }
            } catch (IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }
        }).start();
    }

    /** Elimina el token guardado (logout local) */
    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    /** Devuelve el token JWT almacenado, o null si no existe */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }
}
