package com.budgetpal.budgetppaal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etCurrency;
    private Button btnSignup, btnGoToLogin;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI components
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etCurrency = findViewById(R.id.etCurrency);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        client = new OkHttpClient();

        // Set up click listeners
        btnSignup.setOnClickListener(v -> registerUser());
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String currency = etCurrency.getText().toString().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || currency.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON request body
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

        // Create request (Replace with your actual backend URL)
        Request request = new Request.Builder()
                .url("http://10.0.2.2/budgetpal_api/register.php")
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SignupActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (response.isSuccessful() && jsonResponse.getBoolean("success")) {
                            Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String error = jsonResponse.getString("message");
                            Toast.makeText(SignupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(SignupActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}