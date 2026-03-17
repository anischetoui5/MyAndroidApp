package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthController {

    private Context context;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private AuthListener listener;

    // Add your server URL here
    private static final String BASE_URL = "http://192.168.1.14/budgetpal_api/";

    public interface AuthListener {
        void onLoginSuccess(JSONObject userData);
        void onRegisterSuccess(JSONObject userData);
        void onAuthError(String message);
    }

    public AuthController(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setListener(AuthListener listener) {
        this.listener = listener;
    }

    public void login(String email, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        // CHANGE THIS URL:
        Request request = new Request.Builder()
                .url(BASE_URL + "login.php")  // ← CORRECTED
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    // Add detailed error info
                    String errorMsg = "Network error: " + e.getMessage();
                    if (e.getMessage().contains("Failed to connect")) {
                        errorMsg = "Cannot connect to server at " + BASE_URL ;
                    }
                    listener.onAuthError(errorMsg);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getBoolean("success")) {
                        if (listener != null) {
                            listener.onLoginSuccess(jsonResponse.getJSONObject("user"));
                        }
                    } else {
                        String error = jsonResponse.getString("message");
                        if (listener != null) {
                            listener.onAuthError(error);
                        }
                    }
                } catch (JSONException e) {
                    if (listener != null) {
                        listener.onAuthError("Invalid response from server");
                    }
                }
            }
        });
    }

    public void register(String fullName, String email, String password, String currency) {
        JSONObject json = new JSONObject();
        try {
            json.put("fullName", fullName);
            json.put("email", email);
            json.put("password", password);
            json.put("currency", currency);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        // CHANGE THIS URL TOO:
        Request request = new Request.Builder()
                .url(BASE_URL + "register.php")  // ← CORRECTED
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    // Show the actual URL that failed
                    String errorMsg = "Failed to connect to: " + call.request().url() +
                            "\nError: " + e.getMessage();
                    listener.onAuthError(errorMsg);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getBoolean("success")) {
                        if (listener != null) {
                            listener.onRegisterSuccess(jsonResponse);
                        }
                    } else {
                        String error = jsonResponse.getString("message");
                        if (listener != null) {
                            listener.onAuthError(error);
                        }
                    }
                } catch (JSONException e) {
                    if (listener != null) {
                        listener.onAuthError("Invalid response from server");
                    }
                }
            }
        });
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}