package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.budgetpal.budgetppaal.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CategoryController {

    private static final String BASE_URL = "http://10.0.2.2/budgetpal_api/";

    private final Context context;
    private final OkHttpClient client;
    private final SharedPreferences sharedPreferences;

    private CategoryListener listener;

    // ✅ main thread handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface CategoryListener {
        void onCategoriesLoaded(List<Category> categories);
        void onCategoryAdded(Category category);
        void onCategoryUpdated(Category category);
        void onCategoryDeleted(String categoryId);
        void onError(String message);
    }

    public CategoryController(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setListener(CategoryListener listener) {
        this.listener = listener;
    }

    // ---------------- SAFE CALLBACK HELPERS --------------------
    private void postError(String msg) {
        mainHandler.post(() -> {
            if (listener != null) listener.onError(msg);
        });
    }

    private void postCategoriesLoaded(List<Category> list) {
        mainHandler.post(() -> {
            if (listener != null) listener.onCategoriesLoaded(list);
        });
    }

    private void postCategoryAdded(Category c) {
        mainHandler.post(() -> {
            if (listener != null) listener.onCategoryAdded(c);
        });
    }

    private void postCategoryUpdated(Category c) {
        mainHandler.post(() -> {
            if (listener != null) listener.onCategoryUpdated(c);
        });
    }

    private void postCategoryDeleted(String id) {
        mainHandler.post(() -> {
            if (listener != null) listener.onCategoryDeleted(id);
        });
    }

    // ---------------- LOAD CATEGORIES --------------------
    public void loadCategories() {
        String userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            postError("User not logged in");
            return;
        }

        String url = BASE_URL + "categories.php?userId=" + userId;
        Log.d("CATEGORY_DEBUG", "📡 Loading categories from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CATEGORY_DEBUG", "❌ Network error: " + e.getMessage());
                postError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                Log.d("CATEGORY_DEBUG", "HTTP: " + response.code());
                Log.d("CATEGORY_DEBUG", "RAW: " + body);

                if (body.trim().isEmpty()) {
                    postError("Empty response from server");
                    return;
                }

                // remove BOM if exists
                body = body.replace("\uFEFF", "").trim();

                try {
                    JSONObject obj = new JSONObject(body);

                    if (!obj.optBoolean("success", false)) {
                        postError(obj.optString("message", "API error"));
                        return;
                    }

                    JSONArray arr = obj.optJSONArray("categories");
                    if (arr == null) {
                        postCategoriesLoaded(new ArrayList<>());
                        return;
                    }

                    List<Category> list = new ArrayList<>();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject c = arr.getJSONObject(i);

                        String categoryId = c.has("categoryId") ? c.getString("categoryId")
                                : c.has("categoryID") ? c.getString("categoryID") : "";

                        String dbUserId = c.has("userId") ? c.getString("userId")
                                : c.has("userID") ? c.getString("userID") : userId;

                        String name = c.optString("name", "");
                        double budget = c.optDouble("budget", 0);
                        double spentAmount = c.optDouble("spentAmount", 0);

                        list.add(new Category(categoryId, name, budget, dbUserId, spentAmount));
                    }

                    postCategoriesLoaded(list);

                } catch (JSONException e) {
                    Log.e("CATEGORY_DEBUG", "❌ JSON parse error: " + e.getMessage());
                    String preview = body.substring(0, Math.min(200, body.length()));
                    Log.e("CATEGORY_DEBUG", "❌ Body preview: " + preview);
                    postError("Invalid JSON from server");
                }
            }
        });
    }

    // ---------------- ADD CATEGORY --------------------
    public void addCategory(String name, double budget) {
        String userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            postError("User not logged in");
            return;
        }

        String url = BASE_URL + "addCategory.php";

        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("name", name);
            json.put("budget", budget);
        } catch (JSONException e) {
            postError("JSON error");
            return;
        }

        RequestBody reqBody = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(reqBody)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                body = body.replace("\uFEFF", "").trim();

                if (body.isEmpty()) {
                    postError("Empty response from server");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(body);

                    if (!obj.optBoolean("success", false)) {
                        postError(obj.optString("message", "API error"));
                        return;
                    }

                    JSONObject c = obj.getJSONObject("category");

                    Category category = new Category(
                            String.valueOf(c.optInt("categoryId")),
                            c.optString("name", name),
                            c.optDouble("budget", budget),
                            userId,
                            0
                    );

                    postCategoryAdded(category);

                } catch (JSONException e) {
                    postError("Invalid JSON from server");
                }
            }
        });
    }

    // ---------------- UPDATE CATEGORY --------------------
    public void updateCategory(String categoryId, String name, double budget) {
        JSONObject json = new JSONObject();
        try {
            json.put("categoryId", categoryId);
            json.put("name", name);
            json.put("budget", budget);
        } catch (JSONException e) {
            postError("JSON error");
            return;
        }

        RequestBody reqBody = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "update_category.php")
                .post(reqBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                body = body.replace("\uFEFF", "").trim();

                if (body.isEmpty()) {
                    postError("Empty response from server");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(body);

                    if (!obj.optBoolean("success", false)) {
                        postError(obj.optString("message", "API error"));
                        return;
                    }

                    Category category = new Category(categoryId, name, budget, "", 0);
                    postCategoryUpdated(category);

                } catch (JSONException e) {
                    postError("Invalid JSON from server");
                }
            }
        });
    }

    // ---------------- DELETE CATEGORY --------------------
    public void deleteCategory(String categoryId) {
        JSONObject json = new JSONObject();
        try {
            json.put("categoryId", categoryId);
        } catch (JSONException e) {
            postError("JSON error");
            return;
        }

        RequestBody reqBody = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "delete_category.php")
                .post(reqBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                body = body.replace("\uFEFF", "").trim();

                if (body.isEmpty()) {
                    postError("Empty response from server");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(body);

                    if (!obj.optBoolean("success", false)) {
                        postError(obj.optString("message", "API error"));
                        return;
                    }

                    postCategoryDeleted(categoryId);

                } catch (JSONException e) {
                    postError("Invalid JSON from server");
                }
            }
        });
    }
}
