package com.budgetpal.budgetppaal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class JoinGroupActivity extends AppCompatActivity {

    private EditText etInviteCode;
    private Button btnJoinGroup;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        // Initialize views
        etInviteCode = findViewById(R.id.etInviteCode);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);

        // Initialize HTTP client and shared preferences
        client = new OkHttpClient();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        btnJoinGroup.setOnClickListener(v -> {
            String inviteCode = etInviteCode.getText().toString().trim().toUpperCase();

            if (inviteCode.isEmpty()) {
                Toast.makeText(this, "Please enter an invite code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId.isEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            joinGroup(inviteCode);
        });
    }

    private void joinGroup(String inviteCode) {
        // Create JSON request
        JSONObject json = new JSONObject();
        try {
            json.put("inviteCode", inviteCode);
            json.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        // Use your actual join_group.php endpoint
        String url = "http://10.0.2.2/budgetpal_api/join_group.php";

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        btnJoinGroup.setEnabled(false);
        btnJoinGroup.setText("Joining...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnJoinGroup.setEnabled(true);
                    btnJoinGroup.setText("Join Group");
                    Toast.makeText(JoinGroupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    btnJoinGroup.setEnabled(true);
                    btnJoinGroup.setText("Join Group");

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(JoinGroupActivity.this,
                                    "Successfully joined the group!", Toast.LENGTH_SHORT).show();
                            etInviteCode.setText("");

                            // Navigate back to groups list
                            finish();
                        } else {
                            String error = jsonResponse.getString("message");
                            Toast.makeText(JoinGroupActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(JoinGroupActivity.this,
                                "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}