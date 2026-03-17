package com.budgetpal.budgetppaal;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.budgetpal.budgetppaal.Adapters.GroupAdapter;
import com.budgetpal.budgetppaal.models.Group;

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

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private EditText etGroupName, etGroupBudget, etGroupDescription;
    private Button btnCreateGroup, btnJoinGroup;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private String userId;
    private boolean isFragmentActive = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        etGroupName = view.findViewById(R.id.etGroupName);
        etGroupBudget = view.findViewById(R.id.etGroupBudget);
        etGroupDescription = view.findViewById(R.id.etGroupDescription);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnJoinGroup = view.findViewById(R.id.btnJoinGroup);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Initialize HTTP client and shared preferences
        client = new OkHttpClient();
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0);
        userId = sharedPreferences.getString("userId", "");

        // Setup recycler view
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
        // Load groups when fragment becomes visible
        loadGroups();
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
    }

    private void setupRecyclerView() {
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(requireContext(), groupList);

        // Set click listener for adapter
        groupAdapter.setOnGroupClickListener(new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onGroupClick(int position) {
                if (position >= 0 && position < groupList.size()) {
                    Group group = groupList.get(position);
                    openGroupDetails(group);
                }
            }

            @Override
            public void onInviteClick(int position) {
                if (position >= 0 && position < groupList.size()) {
                    Group group = groupList.get(position);
                    inviteMembers(group);
                }
            }

            @Override
            public void onViewDetailsClick(int position) {
                if (position >= 0 && position < groupList.size()) {
                    Group group = groupList.get(position);
                    openGroupDetails(group);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(groupAdapter);
    }

    private void setupClickListeners() {
        // Create Group button
        btnCreateGroup.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();
            String budgetStr = etGroupBudget.getText().toString().trim();
            String description = etGroupDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (budgetStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter budget", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double budget = Double.parseDouble(budgetStr);
                if (budget <= 0) {
                    Toast.makeText(requireContext(), "Budget must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                createGroup(name, description, budget);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
            }
        });

        // Join Group button
        btnJoinGroup.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), JoinGroupActivity.class);
            startActivity(intent);
        });
    }

    private void loadGroups() {
        if (!isFragmentActive) return;

        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        });

        if (userId == null || userId.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setText("Please log in to view groups");
                tvEmptyState.setVisibility(View.VISIBLE);
            });
            return;
        }

        // Build request to get user's groups
        String url = "http://10.0.2.2/budgetpal_api/groups.php?userId=" + userId;
        Log.d("GroupsDebug", "Loading groups from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!isFragmentActive) return;

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("GroupsDebug", "Network error: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setText("Network error. Please try again.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!isFragmentActive) return;

                String responseBody = response.body().string();
                Log.d("GroupsDebug", "Raw response: " + responseBody);

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.getBoolean("success")) {
                            JSONArray groupsArray = jsonResponse.getJSONArray("groups");
                            Log.d("GroupsDebug", "Found " + groupsArray.length() + " groups");

                            groupList.clear();

                            for (int i = 0; i < groupsArray.length(); i++) {
                                JSONObject groupObj = groupsArray.getJSONObject(i);

                                // Parse values with fallbacks for snake_case
                                int groupId = groupObj.optInt("groupId", groupObj.optInt("group_id", 0));
                                String name = groupObj.optString("name", "");
                                String description = groupObj.optString("description", "");
                                double totalBudget = groupObj.optDouble("totalBudget", groupObj.optDouble("total_budget", 0));
                                double spentAmount = groupObj.optDouble("spentAmount", groupObj.optDouble("spent_amount", 0));
                                int memberCount = groupObj.optInt("memberCount", groupObj.optInt("member_count", 1));
                                String createdAt = groupObj.optString("createdAt", groupObj.optString("created_at", ""));
                                int creatorId = groupObj.optInt("creatorId", groupObj.optInt("creator_id", 0));
                                String currency = groupObj.optString("currency", "USD");
                                String inviteCode = groupObj.optString("inviteCode", groupObj.optString("invite_code", ""));
                                String status = groupObj.optString("status", "active");

                                Group group = new Group(
                                        groupId,
                                        name,
                                        description,
                                        totalBudget,
                                        spentAmount,
                                        memberCount,
                                        createdAt,
                                        creatorId,
                                        currency,
                                        inviteCode,
                                        status
                                );

                                groupList.add(group);
                            }
                            groupAdapter.notifyDataSetChanged();

                            if (groupList.isEmpty()) {
                                tvEmptyState.setText("No groups yet. Create your first group!");
                                tvEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                Log.d("GroupsDebug", "Loaded " + groupList.size() + " groups successfully");
                            }
                        } else {
                            String error = jsonResponse.getString("message");
                            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            tvEmptyState.setText(error);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        Log.e("GroupsDebug", "JSON parse error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error loading groups", Toast.LENGTH_SHORT).show();
                        tvEmptyState.setText("Error loading data");
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void createGroup(String name, String description, double budget) {
        if (!isFragmentActive) return;

        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
        });

        if (userId == null || userId.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Please log in to create a group", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        int creatorId;
        try {
            creatorId = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Create JSON request
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("description", description);
            json.put("totalBudget", budget);
            json.put("creatorId", creatorId);
            json.put("currency", sharedPreferences.getString("currency", "USD"));
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error creating request", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://10.0.2.2/budgetpal_api/groups.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!isFragmentActive) return;

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!isFragmentActive) return;

                String responseBody = response.body().string();
                Log.d("GroupsDebug", "Create group response: " + responseBody);

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.getBoolean("success")) {
                            // Clear input fields
                            etGroupName.setText("");
                            etGroupBudget.setText("");
                            etGroupDescription.setText("");

                            // Parse response
                            JSONObject groupObj = jsonResponse.getJSONObject("group");

                            int groupId = groupObj.optInt("groupId", groupObj.optInt("group_id", 0));
                            String createdAt = groupObj.optString("createdAt", groupObj.optString("created_at", ""));
                            String inviteCode = groupObj.optString("inviteCode", groupObj.optString("invite_code", ""));
                            String status = groupObj.optString("status", "active");
                            String currency = groupObj.optString("currency", sharedPreferences.getString("currency", "USD"));

                            Group newGroup = new Group(
                                    groupId,
                                    name,
                                    description,
                                    budget,
                                    0,
                                    1,
                                    createdAt,
                                    creatorId,
                                    currency,
                                    inviteCode,
                                    status
                            );

                            groupList.add(0, newGroup);
                            groupAdapter.notifyItemInserted(0);
                            recyclerView.scrollToPosition(0);

                            Toast.makeText(requireContext(), "Group created successfully!", Toast.LENGTH_SHORT).show();
                            tvEmptyState.setVisibility(View.GONE);

                            // Show invite code
                            if (inviteCode != null && !inviteCode.isEmpty()) {
                                Toast.makeText(requireContext(), "Invite code: " + inviteCode, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String error = jsonResponse.getString("message");
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupsDebug", "JSON parse error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error creating group", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openGroupDetails(Group group) {
        if (!isFragmentActive) return;

        Intent intent = new Intent(requireActivity(), GroupDetailsActivity.class);
        intent.putExtra("groupId", String.valueOf(group.getGroupId()));
        intent.putExtra("groupName", group.getName());
        startActivity(intent);
    }

    private void inviteMembers(Group group) {
        if (!isFragmentActive) return;

        if (group.getInviteCode() == null || group.getInviteCode().isEmpty()) {
            Toast.makeText(requireContext(), "Invite code not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), InviteActivity.class);
        intent.putExtra("groupId", group.getGroupId());
        intent.putExtra("groupName", group.getName());
        intent.putExtra("inviteCode", group.getInviteCode());
        startActivity(intent);
    }
}