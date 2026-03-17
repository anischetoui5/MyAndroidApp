package com.budgetpal.budgetppaal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.Adapters.CategoryAdapter;
import com.budgetpal.budgetppaal.Adapters.UniversalExpenseAdapter;
import com.budgetpal.budgetppaal.Controllers.DashboardController;
import com.budgetpal.budgetppaal.Controllers.ExpenseController;
import com.budgetpal.budgetppaal.models.Category;
import com.budgetpal.budgetppaal.models.Expense;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements DashboardController.DashboardListener {

    // Existing views
    private TextView tvWelcome, tvBudgetOverview, tvRemainingBudget;

    // New views
    private TextView tvTotalBudget, tvTotalSpent, tvDailyBudget;
    private ProgressBar budgetProgressBar, progressBar;
    private Button btnSetBudget, btnAddExpense;
    private RecyclerView recyclerViewCategories, recyclerViewExpenses;
    private CardView budgetLayout, noBudgetLayout;

    private CategoryAdapter categoryAdapter;
    private UniversalExpenseAdapter expenseAdapter;
    private DashboardController dashboardController;
    private ExpenseController expenseController;

    // Categories list for spinner
    private List<Category> categories = new ArrayList<>();

    private String userId;
    private DecimalFormat format = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize all views
        initializeViews(view);

        // Get user ID
        userId = requireActivity().getSharedPreferences("UserPrefs", 0)
                .getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize controllers
        dashboardController = new DashboardController(requireContext());
        dashboardController.setListener(this);

        expenseController = new ExpenseController(requireContext());
        expenseController.setListener(new ExpenseController.ExpenseListener() {
            @Override
            public void onExpenseAdded(Expense expense) {
                requireActivity().runOnUiThread(() -> {
                    // 1. Add to adapter
                    expenseAdapter.addExpense(expense);

                    // 2. Update local category spent amount
                    updateCategorySpentLocally(expense.getCategoryId(), expense.getAmount());

                    Toast.makeText(getContext(),
                            "✓ Expense added: " + expense.getDescription(),
                            Toast.LENGTH_SHORT).show();

                    // 3. Reload dashboard data
                    loadDashboardData();
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Setup adapters
        setupAdapters();

        // Setup click listeners
        setupClickListeners();

        // Set welcome message
        updateWelcomeMessage();

        // Load dashboard data
        loadDashboardData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when user comes back to this screen
        if (userId != null && !userId.isEmpty()) {
            loadDashboardData();
        }
    }

    private void initializeViews(View view) {
        // Existing views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvBudgetOverview = view.findViewById(R.id.tvBudgetOverview);
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget);

        // New views
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvDailyBudget = view.findViewById(R.id.tvDailyBudget);
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar);
        progressBar = view.findViewById(R.id.progressBar);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);
        budgetLayout = view.findViewById(R.id.budgetLayout);
        noBudgetLayout = view.findViewById(R.id.noBudgetLayout);
    }

    private void updateWelcomeMessage() {
        String userName = requireActivity().getSharedPreferences("UserPrefs", 0)
                .getString("fullName", "User");
        String currentMonth = dateFormat.format(new Date());
        tvWelcome.setText("Welcome, " + userName + "!");
        tvBudgetOverview.setText(currentMonth + " Budget");
    }

    private void setupAdapters() {
        // Categories adapter - use empty constructor
        categoryAdapter = new CategoryAdapter();
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Use UniversalExpenseAdapter for all expense types
        expenseAdapter = new UniversalExpenseAdapter();
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupClickListeners() {
        btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void loadDashboardData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (dashboardController != null) {
            dashboardController.loadDashboard(userId);
        }
    }

    private void showSetBudgetDialog() {
        // TODO: Implement budget dialog
        Toast.makeText(getContext(), "Set budget dialog coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Initialize dialog views
        EditText etDescription = dialogView.findViewById(R.id.etExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Set current date
        final Date[] selectedDate = {new Date()};
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(simpleDateFormat.format(selectedDate[0]));

        // Setup category spinner
        setupCategorySpinner(spinnerCategory);

        // Date picker
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate[0]);

            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate[0] = calendar.getTime();
                        tvSelectedDate.setText(simpleDateFormat.format(selectedDate[0]));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            // Clean description
            description = description.replace("\"", "'")
                    .replace("\\", "/")
                    .replace("\n", " ")
                    .trim();

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Please enter description", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                if (selectedCategory == null) {
                    Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save expense
                expenseController.addExpense(
                        selectedCategory.getCategoryId(),
                        description,
                        amount,
                        selectedDate[0]
                );

                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void setupCategorySpinner(Spinner spinner) {
        // Create adapter for categories
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(getItem(position).getName());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setText(getItem(position).getName());
                return textView;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showReport() {
        // TODO: Show reports/charts
        Toast.makeText(getContext(), "Reports coming soon", Toast.LENGTH_SHORT).show();
    }

    private void updateCategorySpentLocally(String categoryId, double amount) {
        // Find the category in the list and update its spent amount
        for (Category category : categories) {
            if (category.getCategoryId().equals(categoryId)) {
                // Check if category has addToSpent method, otherwise update current_spent
                try {
                    // Try to call addToSpent if it exists
                    category.getClass().getMethod("addToSpent", double.class)
                            .invoke(category, amount);
                } catch (Exception e) {
                    // If addToSpent doesn't exist, try setCurrentSpent
                    try {
                        double currentSpent = (double) category.getClass()
                                .getMethod("getCurrentSpent").invoke(category);
                        category.getClass().getMethod("setCurrentSpent", double.class)
                                .invoke(category, currentSpent + amount);
                    } catch (Exception ex) {
                        // If setter doesn't exist, we'll just reload data
                        return;
                    }
                }

                // Refresh the adapter
                categoryAdapter.updateCategories(categories);
                break;
            }
        }
    }

    // DashboardListener implementation
    @Override
    public void onDashboardLoaded(JSONObject dashboardData) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);

            try {
                JSONObject summary = dashboardData.getJSONObject("summary");

                // Update budget UI
                double totalBudget = summary.getDouble("totalBudget");
                double totalSpent = summary.getDouble("totalSpent");
                double remaining = summary.getDouble("remaining");
                double spentPercentage = summary.getDouble("spentPercentage");
                double dailyBudget = summary.getDouble("dailyBudget");

                // Format the overview text
                String overviewText = String.format(Locale.getDefault(),
                        "Budget: TND%s\nSpent: TND%s\nDaily Budget: TND%s",
                        format.format(totalBudget),
                        format.format(totalSpent),
                        format.format(dailyBudget));

                tvBudgetOverview.setText(overviewText);
                tvRemainingBudget.setText("Remaining: TND" + format.format(remaining));
                tvTotalBudget.setText("TND" + format.format(totalBudget));
                tvTotalSpent.setText("TND" + format.format(totalSpent));
                tvDailyBudget.setText("TND" + format.format(dailyBudget));

                // Set progress bar
                budgetProgressBar.setProgress((int) Math.min(spentPercentage, 100));

                // Show/hide layout based on budget
                if (totalBudget > 0) {
                    budgetLayout.setVisibility(View.VISIBLE);
                    noBudgetLayout.setVisibility(View.GONE);
                } else {
                    budgetLayout.setVisibility(View.GONE);
                    noBudgetLayout.setVisibility(View.VISIBLE);
                }

                // Update categories
                if (dashboardData.has("categories")) {
                    JSONArray categoriesArray = dashboardData.getJSONArray("categories");
                    parseCategories(categoriesArray); // This updates the global categories list
                    categoryAdapter.updateCategories(categories);
                }

                // Update recent expenses (these are personal expenses from PHP API)
                if (dashboardData.has("recentExpenses")) {
                    JSONArray expensesArray = dashboardData.getJSONArray("recentExpenses");
                    List<Expense> expenses = parseExpenses(expensesArray);
                    expenseAdapter.updatePersonalExpenses(expenses);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error parsing dashboard data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseCategories(JSONArray categoriesArray) throws JSONException {
        categories.clear(); // Clear existing categories
        for (int i = 0; i < categoriesArray.length(); i++) {
            JSONObject catJson = categoriesArray.getJSONObject(i);
            Category category = new Category(
                    catJson.getString("categoryID"),
                    catJson.getString("userID"),
                    catJson.getString("name"),
                    catJson.optString("icon", "💰"),
                    catJson.optString("color", "#9E9E9E"),
                    catJson.optDouble("budget_limit", 0),
                    catJson.optDouble("current_spent", 0),
                    catJson.optInt("is_default", 0) == 1
            );
            categories.add(category);
        }
    }

    private List<Expense> parseExpenses(JSONArray expensesArray) throws JSONException {
        List<Expense> expenses = new ArrayList<>();
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < expensesArray.length(); i++) {
            JSONObject expJson = expensesArray.getJSONObject(i);
            String dateStr = expJson.optString("date", "");
            Date date = new Date(); // default to today
            try {
                date = apiDateFormat.parse(dateStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Expense expense = new Expense(
                    expJson.getString("expenseID"),
                    expJson.getString("userID"),
                    expJson.optString("categoryID", ""),
                    expJson.optString("categoryName", "Uncategorized"),
                    expJson.optString("icon", "💰"),
                    expJson.optString("color", "#9E9E9E"),
                    expJson.getDouble("amount"),
                    expJson.optString("description", ""),
                    date,
                    new Date()
            );
            expenses.add(expense);
        }
        return expenses;
    }

    @Override
    public void onError(String message) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        });
    }
}