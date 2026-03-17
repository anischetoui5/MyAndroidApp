package com.budgetpal.budgetppaal;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.budgetpal.budgetppaal.Controllers.ChartController;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.List;

public class ChartsFragment extends Fragment implements ChartController.ChartListener {

    private TextView tvSummary, tvNoData, tvTotalExpenses;
    private ProgressBar progressBar;
    private Spinner spinnerPeriod;
    private LinearLayout chartContainer;
    private RelativeLayout pieChartContainer;
    private ChartController chartController;
    private PieChart pieChart;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);

        // Initialize views
        tvSummary = view.findViewById(R.id.tvSummary);
        tvNoData = view.findViewById(R.id.tvNoData);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        progressBar = view.findViewById(R.id.progressBar);
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod);
        chartContainer = view.findViewById(R.id.chartContainer);
        pieChartContainer = view.findViewById(R.id.pieChartContainer);

        // Create pie chart programmatically with LARGER size
        if (pieChartContainer != null) {
            createAndSetupPieChart();
        }

        // Initialize controller
        chartController = new ChartController(getContext());
        chartController.setChartListener(this);

        // Setup period spinner
        setupPeriodSpinner();

        // Load initial data
        loadChartData("monthly");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Post a runnable to ensure proper layout after view is created
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pieChart != null && pieChartContainer != null) {
                    // Get the actual dimensions of the container
                    int containerWidth = pieChartContainer.getWidth();
                    int containerHeight = pieChartContainer.getHeight();

                    Log.d("PieChartDebug", "Container dimensions: " + containerWidth + "x" + containerHeight);

                    if (containerWidth > 0 && containerHeight > 0) {
                        // Make pie chart VERY LARGE - use 95% of container
                        int targetSize = (int) (Math.min(containerWidth, containerHeight) * 0.95);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                targetSize,
                                targetSize
                        );
                        params.addRule(RelativeLayout.CENTER_IN_PARENT);

                        pieChart.setLayoutParams(params);
                        pieChart.requestLayout();

                        Log.d("PieChartDebug", "Set pie chart to: " + targetSize + "x" + targetSize);

                        // Force a redraw
                        if (pieChart.getData() != null) {
                            pieChart.invalidate();
                        }
                    }
                }
            }
        }, 100); // Small delay to ensure layout is complete
    }

    private void createAndSetupPieChart() {
        pieChart = new PieChart(getContext());

        // Start with a large size
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                600, // Start with 600dp
                600
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        pieChart.setLayoutParams(params);

        // Basic setup
        pieChart.setNoDataText("Loading expense data...");
        pieChart.setNoDataTextColor(Color.GRAY);

        pieChartContainer.addView(pieChart);
    }

    private void setupPeriodSpinner() {
        // Create period options
        String[] periods = {"Daily", "Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                periods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);

        // Set default selection
        spinnerPeriod.setSelection(2); // Monthly

        // Handle selection
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = periods[position].toLowerCase();
                loadChartData(period);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadChartData(String period) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        tvTotalExpenses.setText("Loading...");

        // Clear previous chart if container exists
        if (chartContainer != null) {
            chartContainer.removeAllViews();
        }

        // Reset pie chart
        if (pieChart != null) {
            pieChart.clear();
            pieChart.setNoDataText("Loading data...");
        }

        chartController.loadExpenseData(period);
    }

    // Handle regular chart data
    @Override
    public void onChartDataLoaded(List<ChartController.ChartCategory> chartData, double totalExpenses) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            createChart(chartData, totalExpenses);
        });
    }

    // Handle pie chart data
    @Override
    public void onPieDataLoaded(PieData pieData, double totalExpenses) {
        requireActivity().runOnUiThread(() -> {
            if (pieChart != null && pieData != null) {
                setupPieChart(pieData, totalExpenses);
            }
        });
    }

    private void setupPieChart(PieData pieData, double totalExpenses) {
        try {
            if (pieChart == null || pieData == null) {
                Log.e("PieChart", "PieChart or PieData is null");
                return;
            }

            // Clear any existing data
            pieChart.clear();

            // Apply the data
            pieChart.setData(pieData);

            // 1️⃣ REMOVE ALL TEXT that might be shrinking the chart
            Description description = new Description();
            description.setText("");
            description.setEnabled(false);
            pieChart.setDescription(description);

            // 2️⃣ SIMPLIFY THE CHART - Focus on making it LARGE
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.TRANSPARENT);
            pieChart.setTransparentCircleRadius(0f); // Remove transparent circle
            pieChart.setHoleRadius(40f); // Smaller hole = larger chart area

            // 3️⃣ CENTER TEXT - Make it simple and visible
            pieChart.setDrawCenterText(false);
            DecimalFormat format = new DecimalFormat("#,##0.00");
            String centerText = "Total\nTND" + format.format(totalExpenses);

            SpannableString spannableString = new SpannableString(centerText);
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 6, centerText.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), 6, centerText.length(), 0);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, centerText.length(), 0);


            // 4️⃣ DISABLE ENTRY LABELS - They take up space
            pieChart.setDrawEntryLabels(false);

            // 5️⃣ VALUE TEXT - Make them LARGER and more visible
            pieData.setValueTextSize(12f); // MUCH LARGER
            pieData.setValueTextColor(Color.WHITE);
            pieData.setValueTypeface(Typeface.create("sans-serif", Typeface.BOLD));

            // Custom value formatter - show ALL values
            pieData.setValueFormatter(new ValueFormatter() {
                private final DecimalFormat mFormat = new DecimalFormat("'TND'#,##0");

                @Override
                public String getFormattedValue(float value) {
                    return mFormat.format(value);
                }
            });

            // 6️⃣ SIMPLIFY ROTATION
            pieChart.setRotationEnabled(true);
            pieChart.setRotationAngle(0f);
            pieChart.setHighlightPerTapEnabled(true);

            // 7️⃣ LEGEND - Move it COMPLETELY OUTSIDE or make it smaller
            Legend legend = pieChart.getLegend();
            legend.setEnabled(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
            legend.setTextSize(12f); // Smaller text
            legend.setTextColor(Color.parseColor("#666666"));
            legend.setXEntrySpace(10f);
            legend.setYEntrySpace(5f);
            legend.setYOffset(10f); // Move legend further down
            legend.setFormSize(10f); // Smaller color boxes
            legend.setFormToTextSpace(5f);
            legend.setWordWrapEnabled(true);
            legend.setMaxSizePercent(0.15f); // Limit legend to 25% of chart space

            // 8️⃣ ADD MAXIMUM PADDING for the actual chart area
            pieChart.setExtraOffsets(5, 5, 5, 30); // More bottom offset for legend

            // 9️⃣ DISABLE UNNECESSARY FEATURES
            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setMinAngleForSlices(10f); // Allow smaller slices
            pieChart.setTouchEnabled(true);
            pieChart.setBackgroundColor(Color.TRANSPARENT);

            // 🔟 SIMPLE ANIMATION
            pieChart.animateY(1200, Easing.EaseInOutQuad);

            // 🔟➕1️⃣ CRITICAL: Force the chart to redraw with new size
            pieChart.invalidate();
            pieChart.requestLayout();

            // Log current dimensions
            Log.d("PieChartDebug", "Chart final setup - Width: " + pieChart.getWidth() +
                    ", Height: " + pieChart.getHeight());

        } catch (Exception e) {
            Log.e("PieChart", "Error setting up pie chart: " + e.getMessage(), e);
            // Ultra simple fallback
            if (pieChart != null) {
                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.setDrawHoleEnabled(false); // No hole = more space
                pieData.setValueTextSize(24f); // Make values huge
                pieChart.invalidate();
            }
        }
    }

    private void createChart(List<ChartController.ChartCategory> chartData, double totalExpenses) {
        if (chartContainer == null) {
            Log.e("ChartsFragment", "chartContainer is null!");
            return;
        }

        chartContainer.removeAllViews();

        if (chartData == null || chartData.isEmpty()) {
            tvNoData.setText("No expense data available for the selected period.");
            tvNoData.setVisibility(View.VISIBLE);
            tvSummary.setText("No expenses recorded yet.");
            tvTotalExpenses.setText("Total: TND0.00");
            return;
        }

        DecimalFormat format = new DecimalFormat("#,##0.00");
        tvTotalExpenses.setText("Total Expenses: TND" + format.format(totalExpenses));

        // Add title to chart container
        TextView chartTitle = new TextView(getContext());
        chartTitle.setText("📊 Detailed Breakdown");
        chartTitle.setTextSize(18);
        chartTitle.setTextColor(Color.BLACK);
        chartTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        chartTitle.setPadding(0, 0, 0, 16);
        chartContainer.addView(chartTitle);

        // Create a chart view for each category
        for (ChartController.ChartCategory category : chartData) {
            // Main container for each category
            LinearLayout categoryContainer = new LinearLayout(getContext());
            categoryContainer.setOrientation(LinearLayout.VERTICAL);
            categoryContainer.setPadding(0, 12, 0, 12);

            // Category info row
            LinearLayout infoRow = new LinearLayout(getContext());
            infoRow.setOrientation(LinearLayout.HORIZONTAL);
            infoRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Category name
            TextView tvCategoryName = new TextView(getContext());
            tvCategoryName.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            tvCategoryName.setText(category.label);
            tvCategoryName.setTextSize(16);
            tvCategoryName.setTextColor(Color.BLACK);
            tvCategoryName.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

            // Category amount
            TextView tvCategoryAmount = new TextView(getContext());
            tvCategoryAmount.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tvCategoryAmount.setText("TND" + format.format(category.value));
            tvCategoryAmount.setTextSize(16);
            tvCategoryAmount.setTextColor(Color.parseColor("#333333"));
            tvCategoryAmount.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

            // Add to info row
            infoRow.addView(tvCategoryName);
            infoRow.addView(tvCategoryAmount);

            // Progress bar container
            LinearLayout progressContainer = new LinearLayout(getContext());
            progressContainer.setOrientation(LinearLayout.HORIZONTAL);
            progressContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    28
            ));
            progressContainer.setPadding(0, 8, 0, 8);

            // Colored progress bar
            View progressBarView = new View(getContext());
            int progressWidth = (int) ((category.percentage / 100) * 300); // 300dp max width
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    progressWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            barParams.setMargins(0, 0, 5, 0);
            progressBarView.setLayoutParams(barParams);
            progressBarView.setBackgroundColor(category.color);

            // Empty space for remaining percentage
            View emptySpace = new View(getContext());
            LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                    300 - progressWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            emptySpace.setLayoutParams(spaceParams);
            emptySpace.setBackgroundColor(Color.parseColor("#F0F0F0"));

            // Percentage text
            TextView tvPercentage = new TextView(getContext());
            tvPercentage.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tvPercentage.setText(String.format("%.1f%%", category.percentage));
            tvPercentage.setTextSize(14);
            tvPercentage.setTextColor(Color.GRAY);
            tvPercentage.setPadding(12, 0, 0, 0);

            // Add to progress container
            progressContainer.addView(progressBarView);
            progressContainer.addView(emptySpace);
            progressContainer.addView(tvPercentage);

            // Divider
            View divider = new View(getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            divider.setBackgroundColor(Color.parseColor("#EEEEEE"));

            // Add views to category container
            categoryContainer.addView(infoRow);
            categoryContainer.addView(progressContainer);

            // Add to main container
            chartContainer.addView(categoryContainer);
            chartContainer.addView(divider);
        }

        // Update summary with detailed breakdown
        updateSummaryText(chartData, totalExpenses);
    }

    private void updateSummaryText(List<ChartController.ChartCategory> chartData, double totalExpenses) {
        DecimalFormat format = new DecimalFormat("#,##0.00");

        StringBuilder summary = new StringBuilder();
        summary.append("Category Breakdown:\n\n");

        // Find largest and smallest categories
        ChartController.ChartCategory largest = null;
        ChartController.ChartCategory smallest = null;

        for (ChartController.ChartCategory category : chartData) {
            if (largest == null || category.value > largest.value) {
                largest = category;
            }
            if (smallest == null || category.value < smallest.value) {
                smallest = category;
            }

            summary.append("• ").append(category.label)
                    .append(": TND").append(format.format(category.value))
                    .append(" (").append(String.format("%.1f", category.percentage))
                    .append("%)\n");
        }

        // Add insights
        if (largest != null && smallest != null) {
            summary.append("\n📊 Insights:\n");
            summary.append("Highest: ").append(largest.label)
                    .append(" (TND").append(format.format(largest.value)).append(")\n");
            summary.append("Lowest: ").append(smallest.label)
                    .append(" (TND").append(format.format(smallest.value)).append(")\n");

            double average = totalExpenses / chartData.size();
            summary.append("Average: TND").append(format.format(average))
                    .append(" per category");
        }

        tvSummary.setText(summary.toString());
    }

    @Override
    public void onError(String message) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            tvNoData.setText("Failed to load chart data. Please try again.");
            tvNoData.setVisibility(View.VISIBLE);
            tvSummary.setText("Error loading data. Please check your connection.");
            tvTotalExpenses.setText("Error");

            if (pieChart != null) {
                pieChart.clear();
                pieChart.setNoDataText("Error loading data");
                pieChart.invalidate();
            }
        });
    }
}