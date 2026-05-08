package com.gymtrack.app.network;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClientRepository {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/client";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson;
    private final AuthRepository authRepository;

    public ClientRepository(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.authRepository = new AuthRepository(context);
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getProfile(Callback<JsonObject> callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/profile")
                        .addHeader("Authorization", "Bearer " + authRepository.getToken())
                        .get()
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        callback.onSuccess(json);
                    } else {
                        callback.onError("Error al obtener perfil");
                    }
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void updateProfile(JsonObject data, Callback<Void> callback) {
        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(data.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/profile")
                        .addHeader("Authorization", "Bearer " + authRepository.getToken())
                        .put(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Error al actualizar perfil");
                    }
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void linkTrainer(String code, Callback<String> callback) {
        new Thread(() -> {
            try {
                RequestBody emptyBody = RequestBody.create("", null);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/link-trainer/" + code)
                        .addHeader("Authorization", "Bearer " + authRepository.getToken())
                        .post(emptyBody)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String respBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess(respBody);
                    } else {
                        callback.onError(respBody.isEmpty() ? "Código inválido" : respBody);
                    }
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
