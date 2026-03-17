package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardController {

    public interface DashboardListener {
        void onDashboardLoaded(JSONObject dashboardData);
        void onError(String message);
    }

    private Context context;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private DashboardListener listener;

    // ✅ FOR EMULATOR: Use 10.0.2.2
    private static final String BASE_URL = "http://10.0.2.2/budgetpal_api/";

    public DashboardController(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setListener(DashboardListener listener) {
        this.listener = listener;
    }

    public void loadDashboard(String userId) {
        String url = BASE_URL + "get_dashboard.php?userID=" + userId;

        Log.d("EMULATOR_DEBUG", "📡 Emulator connecting to: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EMULATOR_DEBUG", "❌ Emulator connection failed: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Cannot connect from emulator: " + e.getMessage() +
                            "\nMake sure: 1. XAMPP is running 2. PHP files are in budgetpal_api folder");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("EMULATOR_DEBUG", "✅ Response code: " + response.code());
                String responseBody = response.body().string();
                Log.d("EMULATOR_DEBUG", "Response length: " + responseBody.length());

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getBoolean("success")) {
                        if (listener != null) {
                            listener.onDashboardLoaded(jsonResponse.getJSONObject("data"));
                        }
                    } else {
                        String error = jsonResponse.getString("message");
                        Log.e("EMULATOR_DEBUG", "API error: " + error);
                        if (listener != null) {
                            listener.onError("API Error: " + error);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("EMULATOR_DEBUG", "JSON parse error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Invalid JSON response");
                    }
                }
            }
        });
    }

    // Test connection method
    public void testEmulatorConnection() {
        String testUrl = BASE_URL + "test_connection.php";
        Log.d("EMULATOR_TEST", "Testing: " + testUrl);

        Request request = new Request.Builder()
                .url(testUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EMULATOR_TEST", "❌ Test failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("EMULATOR_TEST", "✅ Test success: " + body);
            }
        });
    }
}