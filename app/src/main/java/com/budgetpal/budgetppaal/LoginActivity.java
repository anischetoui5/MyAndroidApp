package com.budgetpal.budgetppaal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToSignup;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnGoToSignup);

        client = new OkHttpClient();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMain();
        }

        // Set up click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON request body
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

        // ✅ FIXED: Use 10.0.2.2 for emulator
        String url = "http://10.0.2.2/budgetpal_api/login.php";

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Cannot connect to server: " + e.getMessage() +
                                    "\nMake sure XAMPP is running on your computer",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (response.isSuccessful() && jsonResponse.getBoolean("success")) {
                            // Save user data locally
                            JSONObject user = jsonResponse.getJSONObject("user");
                            saveUserData(
                                    user.getString("userID"),
                                    user.getString("fullName"),
                                    user.getString("email"),
                                    user.getString("currency")
                            );

                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            String error = jsonResponse.getString("message");
                            Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveUserData(String userId, String fullName, String email, String currency) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.putString("fullName", fullName);
        editor.putString("email", email);
        editor.putString("currency", currency);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}