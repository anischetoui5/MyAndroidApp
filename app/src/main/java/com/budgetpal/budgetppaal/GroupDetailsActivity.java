    package com.budgetpal.budgetppaal;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.LinearLayout;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.cardview.widget.CardView;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.cardview.widget.CardView;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.budgetpal.budgetppaal.Adapters.MemberAdapter;
    import com.budgetpal.budgetppaal.Adapters.ExpenseAdapter;
    import com.budgetpal.budgetppaal.models.Group;
    import com.budgetpal.budgetppaal.models.GroupMember;
    import com.budgetpal.budgetppaal.models.GroupExpense;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.text.DecimalFormat;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.MediaType;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;




    public class GroupDetailsActivity extends AppCompatActivity {

        private TextView tvGroupName, tvTotalBudget, tvSpentAmount, tvRemainingBudget, tvMemberCount;
        private TextView tvAverageSpent, tvStatus, tvNoMembers, tvNoExpenses;
        private ProgressBar progressBar, budgetProgressBar;
        private Button btnAddExpense, btnInviteMore, btnLeaveGroup;
        private CardView membersContainer, expensesContainer;
        private RecyclerView recyclerViewMembers, recyclerViewExpenses;

        private MemberAdapter memberAdapter;
        private ExpenseAdapter expenseAdapter;
        private List<GroupMember> memberList;
        private List<GroupExpense> expenseList;

        private OkHttpClient client;
        private String groupId;
        private String userId;
        private Group group;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_group_details);

            // Get group data from intent
            Intent intent = getIntent();
            groupId = intent.getStringExtra("groupId");
            String groupName = intent.getStringExtra("groupName");

            // Initialize views
            initializeViews();

            Log.d("GroupDetails", "tvGroupName = " + tvGroupName);
            Log.d("GroupDetails", "tvTotalBudget = " + tvTotalBudget);


            // Initialize data
            client = new OkHttpClient();
            userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
            memberList = new ArrayList<>();
            expenseList = new ArrayList<>();

            // Set group name (temporary, until API loads full group)
            tvGroupName.setText(groupName);

            // Setup adapters
            setupAdapters();

            // Setup click listeners
            setupClickListeners();

            // Load group details from API
            loadGroupDetails();
        }


        private void initializeViews() {
            tvGroupName = findViewById(R.id.tvGroupName);
            tvTotalBudget = findViewById(R.id.tvTotalBudget);
            tvSpentAmount = findViewById(R.id.tvSpentAmount);
            tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
            tvMemberCount = findViewById(R.id.tvMemberCount);
            tvAverageSpent = findViewById(R.id.tvAverageSpent);
            tvStatus = findViewById(R.id.tvStatus);
            tvNoMembers = findViewById(R.id.tvNoMembers);
            tvNoExpenses = findViewById(R.id.tvNoExpenses);

            progressBar = findViewById(R.id.progressBar);
            budgetProgressBar = findViewById(R.id.budgetProgressBar);

            btnAddExpense = findViewById(R.id.btnAddExpense);
            btnInviteMore = findViewById(R.id.btnInviteMore);
            btnLeaveGroup = findViewById(R.id.btnLeaveGroup);

            membersContainer = findViewById(R.id.membersContainer);
            expensesContainer = findViewById(R.id.expensesContainer);

            recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
            recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        }

        private void setupAdapters() {
            // Members adapter
            memberAdapter = new MemberAdapter(memberList);
            recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewMembers.setAdapter(memberAdapter);

            // Expenses adapter
            expenseAdapter = new ExpenseAdapter(expenseList);
            recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewExpenses.setAdapter(expenseAdapter);
        }

        private void setupClickListeners() {
            btnAddExpense.setOnClickListener(v -> openAddExpenseDialog());
            btnInviteMore.setOnClickListener(v -> openInviteActivity());
            btnLeaveGroup.setOnClickListener(v -> showLeaveGroupConfirmation());
        }

        private void loadGroupDetails() {
            progressBar.setVisibility(View.VISIBLE);

            Log.d("GroupDetails", "Loading group details for groupId: " + groupId + ", userId: " + userId);

            String url = "http://10.0.2.2/budgetpal_api/group_details.php?groupId=" + groupId + "&userId=" + userId;
            Log.d("GroupDetails", "API URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e("GroupDetails", "Network error: " + e.getMessage());
                        Toast.makeText(GroupDetailsActivity.this, "Failed to load group details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("GroupDetails", "Raw response: " + responseBody);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            Log.d("GroupDetails", "Parsed JSON: " + jsonResponse.toString());

                            if (jsonResponse.getBoolean("success")) {
                                parseGroupData(jsonResponse);
                            } else {
                                String error = jsonResponse.getString("message");
                                Log.e("GroupDetails", "API error: " + error);
                                Toast.makeText(GroupDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("GroupDetails", "JSON parse error: " + e.getMessage());
                            Toast.makeText(GroupDetailsActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        private void parseGroupData(JSONObject jsonResponse) throws JSONException {
            // Parse group info - use camelCase keys
            JSONObject groupObj = jsonResponse.getJSONObject("group");
            this.group = new Group(
                    groupObj.getInt("groupId"),
                    groupObj.getString("name"),
                    groupObj.getString("description"),
                    groupObj.getDouble("totalBudget"),
                    groupObj.getDouble("spentAmount"),
                    groupObj.getInt("memberCount"),
                    groupObj.optString("createdAt", groupObj.optString("created_at", "")),
                    groupObj.getInt("creatorId"),
                    groupObj.getString("currency"),
                    groupObj.optString("inviteCode", "")
            );


            // Update UI with group data
            runOnUiThread(() -> updateGroupUI());

            // Parse members - FIXED key names
            memberList.clear();
            if (jsonResponse.has("members")) {
                JSONArray membersArray = jsonResponse.getJSONArray("members");
                Log.d("GroupDetails", "Found " + membersArray.length() + " members");

                for (int i = 0; i < membersArray.length(); i++) {
                    JSONObject memberObj = membersArray.getJSONObject(i);
                    Log.d("GroupDetails", "Member " + i + ": " + memberObj.toString());

                    // Use camelCase keys matching PHP response
                    GroupMember member = new GroupMember(
                            String.valueOf(memberObj.getInt("userId")),
                            memberObj.getString("userName"),
                            memberObj.getString("email"),
                            memberObj.getDouble("paidAmount"),    // Not totalSpent
                            memberObj.getDouble("owedAmount"),    // Not shareAmount
                            memberObj.getDouble("balance")
                    );

                    memberList.add(member);
                }
                memberAdapter.notifyDataSetChanged();
                tvNoMembers.setVisibility(View.GONE);
            } else {
                Log.d("GroupDetails", "No members key in response");
                tvNoMembers.setVisibility(View.VISIBLE);
            }

            // Parse expenses - FIXED key names
            expenseList.clear();
            if (jsonResponse.has("expenses")) {
                JSONArray expensesArray = jsonResponse.getJSONArray("expenses");
                Log.d("GroupDetails", "Found " + expensesArray.length() + " expenses");

                for (int i = 0; i < expensesArray.length(); i++) {
                    JSONObject expenseObj = expensesArray.getJSONObject(i);

                    GroupExpense expense = new GroupExpense(
                            String.valueOf(expenseObj.getInt("expenseId")),
                            expenseObj.getString("description"),
                            expenseObj.getDouble("amount"),
                            expenseObj.getString("payerName"),
                            expenseObj.getString("category"),
                            expenseObj.getString("date"),
                            expenseObj.getBoolean("isSettled")
                    );

                    expenseList.add(expense);
                }
                expenseAdapter.notifyDataSetChanged();
                tvNoExpenses.setVisibility(View.GONE);
            } else {
                Log.d("GroupDetails", "No expenses key in response");
                tvNoExpenses.setVisibility(View.VISIBLE);
            }
        }

        private void updateGroupUI() {
            DecimalFormat format = new DecimalFormat("#,##0.00");
            String currency = group.getCurrency();

            // Update basic info
            tvTotalBudget.setText("Total Budget: " + currency + " " + format.format(group.getTotalBudget()));
            tvSpentAmount.setText("Spent: " + currency + " " + format.format(group.getSpentAmount()));
            tvRemainingBudget.setText("Remaining: " + currency + " " + format.format(group.getRemainingBudget()));
            tvMemberCount.setText("Members: " + group.getMemberCount());

            // Calculate average spent per person
            double averageSpent = group.getMemberCount() > 0 ? group.getSpentAmount() / group.getMemberCount() : 0;
            tvAverageSpent.setText("Average per person: " + currency + " " + format.format(averageSpent));

            // Update progress bar
            int progress = (group.getTotalBudget() > 0) ?
                    (int) ((group.getSpentAmount() / group.getTotalBudget()) * 100) : 0;
            budgetProgressBar.setProgress(progress);

            // Update status
            if (progress < 70) {
                tvStatus.setText("On Track");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (progress < 95) {
                tvStatus.setText("Near Limit");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tvStatus.setText("Over Budget");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Show/Hide buttons based on user role
            boolean isCreator = userId.equals(String.valueOf(group.getCreatorId()));
            btnLeaveGroup.setText(isCreator ? "Delete Group" : "Leave Group");
        }

        private void openAddExpenseDialog() {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_group_expense, null);

            EditText etDescription = dialogView.findViewById(R.id.etExpenseDescription);
            EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
            EditText etCategory = dialogView.findViewById(R.id.etExpenseCategory);
            Button btnConfirm = dialogView.findViewById(R.id.btnAddExpenseConfirm);

            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            btnConfirm.setOnClickListener(v -> {
                String description = etDescription.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                String category = etCategory.getText().toString().trim();

                if (description.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                addGroupExpense(description, amount, category);
                dialog.dismiss();
            });

            dialog.show();
        }
        private void addGroupExpense(String description, double amount, String category) {
            if (group == null) return;

            progressBar.setVisibility(View.VISIBLE);

            JSONObject json = new JSONObject();
            try {
                json.put("groupId", group.getGroupId());
                json.put("userId", userId);
                json.put("description", description);
                json.put("amount", amount);
                json.put("category", category);
                json.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date()));
            } catch (JSONException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error creating expense data", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://10.0.2.2/budgetpal_api/group_add_expense.php") // <-- your new API endpoint
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GroupDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    Log.d("AddExpenseDebug", "Raw response from server: " + responseBody);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                JSONObject expenseObj = jsonResponse.getJSONObject("expense");

                                GroupExpense newExpense = new GroupExpense(
                                        String.valueOf(expenseObj.getInt("expenseId")),
                                        expenseObj.getString("description"),
                                        expenseObj.getDouble("amount"),
                                        expenseObj.getString("payerName"),
                                        expenseObj.getString("category"),
                                        expenseObj.getString("date"),
                                        expenseObj.getBoolean("isSettled")
                                );

                                expenseList.add(0, newExpense);
                                expenseAdapter.notifyItemInserted(0);
                                recyclerViewExpenses.scrollToPosition(0);

                                // Update group spent amount
                                group.setSpentAmount(group.getSpentAmount() + amount);
                                updateGroupUI();

                                Toast.makeText(GroupDetailsActivity.this, "Expense added!", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = jsonResponse.getString("message");
                                Toast.makeText(GroupDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(GroupDetailsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }


        private void openInviteActivity() {
            if (group == null) {
                Toast.makeText(this, "Group data not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, InviteActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", group.getName() != null ? group.getName() : "");
            if (group.getInviteCode() != null && !group.getInviteCode().isEmpty()) {
                intent.putExtra("inviteCode", group.getInviteCode());
            }
            startActivity(intent);
        }

        private void showLeaveGroupConfirmation() {
            if (group == null) {
                Toast.makeText(this, "Group data not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCreator = userId.equals(String.valueOf(group.getCreatorId()));
            String title = isCreator ? "Delete Group" : "Leave Group";
            String message = isCreator
                    ? "Are you sure you want to delete this group? This action cannot be undone."
                    : "Are you sure you want to leave this group?";

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes", (dialog, which) -> leaveOrDeleteGroup())
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        private void leaveOrDeleteGroup() {
            progressBar.setVisibility(View.VISIBLE);

            JSONObject json = new JSONObject();
            try {
                // IMPORTANT: send integers if possible
                json.put("groupId", Integer.parseInt(groupId));
                json.put("userId", Integer.parseInt(userId));
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Invalid groupId/userId", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            String url = "http://10.0.2.2/budgetpal_api/group_leave.php";

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GroupDetailsActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = (response.body() != null) ? response.body().string() : "";

                    Log.d("LeaveGroup", "HTTP code: " + response.code());
                    Log.d("LeaveGroup", "RAW response: " + responseBody);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);

                        // ✅ Fix BOM + whitespace
                        String clean = responseBody.replace("\uFEFF", "").trim();

                        // ✅ If server returned HTML / warning / empty
                        if (clean.isEmpty()) {
                            Toast.makeText(GroupDetailsActivity.this, "Empty response from server", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!clean.startsWith("{")) {
                            String preview = clean.substring(0, Math.min(150, clean.length()));
                            Toast.makeText(GroupDetailsActivity.this, "Non-JSON server response: " + preview, Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            JSONObject res = new JSONObject(clean);
                            boolean success = res.optBoolean("success", false);
                            String message = res.optString("message", "Done");

                            Toast.makeText(GroupDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                finish(); // go back
                            }
                        } catch (JSONException e) {
                            Toast.makeText(GroupDetailsActivity.this, "Bad JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }