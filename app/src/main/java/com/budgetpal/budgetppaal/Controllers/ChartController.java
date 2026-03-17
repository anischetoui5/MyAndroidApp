package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChartController {

    public interface ChartListener {
        void onChartDataLoaded(List<ChartCategory> chartData, double totalExpenses);
        void onPieDataLoaded(PieData pieData, double totalExpenses);
        void onError(String message);
    }

    // Model class for chart data
    public static class ChartCategory {
        public String label;
        public float value;
        public int color;
        public double percentage;
        public String categoryId;

        public ChartCategory(String categoryId, String label, float value, int color, double percentage) {
            this.categoryId = categoryId;
            this.label = label;
            this.value = value;
            this.color = color;
            this.percentage = percentage;
        }
    }

    private Context context;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private ChartListener listener;

    public ChartController(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setChartListener(ChartListener listener) {
        this.listener = listener;
    }

    public void loadExpenseData(String period) {
        String userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        // ✅ Use your local IP or 10.0.2.2 for emulator
        String baseUrl = "http://10.0.2.2/budgetpal_api/"; // For emulator
        // String baseUrl = "http://192.168.1.14/budgetpal_api/"; // For real device

        String url = baseUrl + "chart_data.php?userId=" + userId + "&period=" + period;
        Log.d("CHART_DEBUG", "Loading chart data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CHART_DEBUG", "Network error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("CHART_DEBUG", "Response received: " + responseBody.substring(0, Math.min(200, responseBody.length())));

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.getBoolean("success")) {
                        String error = jsonResponse.getString("message");
                        Log.e("CHART_DEBUG", "API error: " + error);
                        if (listener != null) {
                            listener.onError(error);
                        }
                        return;
                    }

                    List<ChartCategory> chartData = new ArrayList<>();
                    List<PieEntry> pieEntries = new ArrayList<>();
                    List<Integer> pieColors = new ArrayList<>();

                    double totalExpenses = jsonResponse.getDouble("totalExpenses");
                    JSONArray categoriesArray = jsonResponse.getJSONArray("categories");

                    for (int i = 0; i < categoriesArray.length(); i++) {
                        JSONObject category = categoriesArray.getJSONObject(i);

                        String categoryId = category.getString("categoryID");
                        String categoryName = category.getString("categoryName");
                        float categoryAmount = (float) category.getDouble("totalAmount");
                        double percentage = category.optDouble("percentage", 0);

                        // Get color from database or use default
                        int color;
                        try {
                            String colorHex = category.optString("color", "");
                            if (!colorHex.isEmpty() && colorHex.startsWith("#")) {
                                color = Color.parseColor(colorHex);
                            } else {
                                color = getColorForIndex(i);
                            }
                        } catch (Exception e) {
                            color = getColorForIndex(i);
                        }

                        // Add to chart data
                        ChartCategory chartCategory = new ChartCategory(
                                categoryId,
                                categoryName,
                                categoryAmount,
                                color,
                                percentage
                        );
                        chartData.add(chartCategory);

                        // Add to pie chart data
                        pieEntries.add(new PieEntry(categoryAmount, categoryName));
                        pieColors.add(color);
                    }

                    Log.d("CHART_DEBUG", "Loaded " + chartData.size() + " categories, total: TND" + totalExpenses);

                    // Create PieData
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expenses by Category");
                    pieDataSet.setColors(pieColors);
                    pieDataSet.setValueTextSize(12f);
                    pieDataSet.setValueTextColor(Color.WHITE);

                    PieData pieData = new PieData(pieDataSet);

                    // ✅ FIXED: Use proper ValueFormatter (Anonymous class)
                    pieData.setValueFormatter(new ValueFormatter() {
                        private final DecimalFormat mFormat = new DecimalFormat("TND#,##0.00");

                        @Override
                        public String getFormattedValue(float value) {
                            return mFormat.format(value);
                        }
                    });

                    // Notify listeners
                    if (listener != null) {
                        listener.onChartDataLoaded(chartData, totalExpenses);
                        listener.onPieDataLoaded(pieData, totalExpenses);
                    }

                } catch (JSONException e) {
                    Log.e("CHART_DEBUG", "JSON parse error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Error parsing response");
                    }
                } catch (Exception e) {
                    Log.e("CHART_DEBUG", "Other error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Error loading chart data");
                    }
                }
            }
        });
    }

    private int getColorForIndex(int index) {
        int[] colors = {
                Color.parseColor("#FF6B6B"), // Red
                Color.parseColor("#4ECDC4"), // Teal
                Color.parseColor("#FFD166"), // Yellow
                Color.parseColor("#06D6A0"), // Green
                Color.parseColor("#118AB2"), // Blue
                Color.parseColor("#EF476F"), // Pink
                Color.parseColor("#7209B7"), // Purple
                Color.parseColor("#F3722C"), // Orange
                Color.parseColor("#277DA1"), // Dark Blue
                Color.parseColor("#43AA8B"), // Dark Green
                Color.parseColor("#F15BB5"), // Magenta
                Color.parseColor("#9B5DE5"), // Light Purple
                Color.parseColor("#00BBF9"), // Light Blue
                Color.parseColor("#00F5D4")  // Cyan
        };
        return colors[index % colors.length];
    }
}