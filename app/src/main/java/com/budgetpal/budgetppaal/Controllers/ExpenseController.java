package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExpenseController {

    private static final String BASE_URL = "http://10.0.2.2/budgetpal_api/";
    private Context context;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private ExpenseListener listener;

    public interface ExpenseListener {
        void onExpenseAdded(com.budgetpal.budgetppaal.models.Expense expense);
        void onError(String message);
    }

    public ExpenseController(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setListener(ExpenseListener listener) {
        this.listener = listener;
    }
    public void addExpense(String categoryId, String description, double amount, Date date) {
        String userId = sharedPreferences.getString("userId", "");

        Log.d("EXPENSE_FIX", "=== ADD EXPENSE ===");
        Log.d("EXPENSE_FIX", "User: " + userId);
        Log.d("EXPENSE_FIX", "Category: " + categoryId);
        Log.d("EXPENSE_FIX", "Desc: " + description);
        Log.d("EXPENSE_FIX", "Amount: " + amount);

        if (userId.isEmpty()) {
            if (listener != null) listener.onError("User not logged in");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(date);
        Log.d("EXPENSE_FIX", "Date: " + dateStr);

        // ✅ FIX: Create JSON properly
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("categoryId", categoryId);
            json.put("description", description);
            json.put("amount", amount);
            json.put("date", dateStr);

            Log.d("EXPENSE_FIX", "✅ JSON: " + json.toString());
        } catch (JSONException e) {
            Log.e("EXPENSE_FIX", "❌ JSON Error: " + e.getMessage());
            if (listener != null) listener.onError("JSON creation failed");
            return;
        }

        // ✅ FIX: Create RequestBody correctly
        String jsonString = json.toString();
        Log.d("EXPENSE_FIX", "JSON string: " + jsonString);
        Log.d("EXPENSE_FIX", "JSON length: " + jsonString.length());

        // ⚠️ IMPORTANT: Use the String directly, not MediaType.parse
        RequestBody body = RequestBody.create(
                jsonString,
                MediaType.parse("application/json; charset=utf-8")
        );

        // Alternative: Try this if above doesn't work
        // RequestBody body = new FormBody.Builder()
        //     .add("json", jsonString)
        //     .build();

        String url = BASE_URL + "add_expense.php";
        Log.d("EXPENSE_FIX", "URL: " + url);

        // ✅ FIX: Build request with proper headers
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        // Log the full request
        Log.d("EXPENSE_FIX", "Request method: " + request.method());
        Log.d("EXPENSE_FIX", "Request headers: " + request.headers());
        try {
            Log.d("EXPENSE_FIX", "Request body size: " + (body.contentLength()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EXPENSE_FIX", "❌ Network error: " + e.getMessage());
                e.printStackTrace();
                if (listener != null) listener.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("EXPENSE_FIX", "Response code: " + response.code());
                Log.d("EXPENSE_FIX", "Response body: " + responseBody);

                // Check for empty response
                if (responseBody == null || responseBody.isEmpty()) {
                    Log.e("EXPENSE_FIX", "❌ Empty response from server");
                    if (listener != null) listener.onError("Empty response from server");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(responseBody);

                    if (obj.getBoolean("success")) {
                        JSONObject exp = obj.getJSONObject("expense");
                        com.budgetpal.budgetppaal.models.Expense expense =
                                new com.budgetpal.budgetppaal.models.Expense(
                                        exp.getString("expenseID"),
                                        exp.getString("userID"),
                                        exp.getString("categoryID"),
                                        exp.optString("categoryName", "Uncategorized"),
                                        exp.optString("icon", "💰"),
                                        exp.optString("color", "#9E9E9E"),
                                        exp.getDouble("amount"),
                                        exp.getString("description"),
                                        dateFormat.parse(exp.getString("date")),
                                        new Date()
                                );

                        Log.d("EXPENSE_FIX", "✅ Success! Added expense: " + expense.getDescription());
                        if (listener != null) listener.onExpenseAdded(expense);

                    } else {
                        String error = obj.getString("message");
                        Log.e("EXPENSE_FIX", "❌ API error: " + error);
                        if (listener != null) listener.onError(error);
                    }

                } catch (JSONException e) {
                    Log.e("EXPENSE_FIX", "❌ JSON parse error: " + e.getMessage());
                    Log.e("EXPENSE_FIX", "❌ Response was: " + responseBody);
                    if (listener != null) listener.onError("Invalid JSON from server");
                } catch (Exception e) {
                    Log.e("EXPENSE_FIX", "❌ Other error: " + e.getMessage());
                    e.printStackTrace();
                    if (listener != null) listener.onError("Error: " + e.getMessage());
                }
            }

        });
    }

    private String getCategoryName(String categoryId) {
        // Map category IDs to names
        switch (categoryId) {
            case "1": return "Food";
            case "2": return "Transport";
            case "3": return "Entertainment";
            case "4": return "Shopping";
            default: return "Other";
        }
    }
}