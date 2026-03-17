package com.budgetpal.budgetppaal.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.budgetpal.budgetppaal.models.Group;

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

public class GroupController {

    public interface GroupListener {
        void onGroupsLoaded(List<Group> groups);
        void onGroupCreated(Group group, String inviteCode);
        void onGroupJoined(Group group);
        void onError(String message);
    }

    private Context context;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private GroupListener listener;

    public GroupController(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void setGroupListener(GroupListener listener) {
        this.listener = listener;
    }

    // Get user's groups
    public void loadUserGroups() {
        String userId = sharedPreferences.getString("userId", "");
        String baseUrl = "http://10.0.2.2/budgetpal_api/";

        if (userId.isEmpty()) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        String url = baseUrl + "groups.php?userId=" + userId;
        Log.d("GROUP_DEBUG", "Loading groups from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GROUP_DEBUG", "Network error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GROUP_DEBUG", "Groups response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.getBoolean("success")) {
                        String error = jsonResponse.getString("message");
                        if (listener != null) {
                            listener.onError(error);
                        }
                        return;
                    }

                    List<Group> groups = new ArrayList<>();
                    JSONArray groupsArray = jsonResponse.getJSONArray("groups");

                    for (int i = 0; i < groupsArray.length(); i++) {
                        JSONObject groupObj = groupsArray.getJSONObject(i);

                        // Parse values, handling potential nulls and different key names
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
                                groupId,                    // int
                                name,                       // String
                                description,                // String
                                totalBudget,                // double
                                spentAmount,                // double
                                memberCount,                // int
                                createdAt,                  // String (not null)
                                creatorId,                  // int
                                currency,                   // String
                                inviteCode,                 // String
                                status                      // String
                        );

                        groups.add(group);
                    }

                    if (listener != null) {
                        listener.onGroupsLoaded(groups);
                    }

                } catch (JSONException e) {
                    Log.e("GROUP_DEBUG", "JSON parse error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Error parsing response");
                    }
                }
            }
        });
    }

    // Create a new group
    public void createGroup(String name, String description, double totalBudget) {
        String userIdStr = sharedPreferences.getString("userId", "");
        String currency = sharedPreferences.getString("currency", "USD");
        String baseUrl = "http://10.0.2.2/budgetpal_api/";

        if (userIdStr.isEmpty()) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        // Parse userId to integer
        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            if (listener != null) {
                listener.onError("Invalid user ID format");
            }
            return;
        }

        // Create JSON request
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("description", description);
            json.put("totalBudget", totalBudget);
            json.put("creatorId", userId);
            json.put("currency", currency);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        String url = baseUrl + "groups.php";
        Log.d("GROUP_DEBUG", "Creating group at: " + url);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GROUP_DEBUG", "Create group error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GROUP_DEBUG", "Create group response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.getBoolean("success")) {
                        String error = jsonResponse.getString("message");
                        if (listener != null) {
                            listener.onError(error);
                        }
                        return;
                    }

                    JSONObject groupObj = jsonResponse.getJSONObject("group");

                    // Parse values, handling different key names
                    int groupId = groupObj.optInt("groupId", groupObj.optInt("group_id", 0));
                    String createdAt = groupObj.optString("createdAt", groupObj.optString("created_at", ""));
                    String inviteCode = groupObj.optString("inviteCode", groupObj.optString("invite_code", ""));
                    String status = groupObj.optString("status", "active");

                    Group newGroup = new Group(
                            groupId,                    // int
                            name,                       // String
                            description,                // String
                            totalBudget,                // double
                            0,                          // double - initial spent
                            1,                          // int - starting member count
                            createdAt,                  // String
                            userId,                     // int - creatorId
                            currency,                   // String
                            inviteCode,                 // String
                            status                      // String
                    );

                    if (listener != null) {
                        listener.onGroupCreated(newGroup, inviteCode);
                    }

                } catch (JSONException e) {
                    Log.e("GROUP_DEBUG", "JSON parse error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Error parsing response");
                    }
                }
            }
        });
    }

    // Join a group with invite code
    public void joinGroup(String inviteCode) {
        String userIdStr = sharedPreferences.getString("userId", "");
        String baseUrl = "http://10.0.2.2/budgetpal_api/";

        if (userIdStr.isEmpty()) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        // Parse userId to integer
        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            if (listener != null) {
                listener.onError("Invalid user ID format");
            }
            return;
        }

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

        String url = baseUrl + "join_group.php";
        Log.d("GROUP_DEBUG", "Joining group at: " + url);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GROUP_DEBUG", "Join group error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GROUP_DEBUG", "Join group response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.getBoolean("success")) {
                        String error = jsonResponse.getString("message");
                        if (listener != null) {
                            listener.onError(error);
                        }
                        return;
                    }

                    JSONObject groupObj = jsonResponse.getJSONObject("group");

                    // Parse values, handling different key names
                    int groupId = groupObj.optInt("groupId", groupObj.optInt("group_id", 0));
                    String name = groupObj.optString("name", "");
                    String description = groupObj.optString("description", "");
                    double totalBudget = groupObj.optDouble("totalBudget", groupObj.optDouble("total_budget", 0));
                    double spentAmount = groupObj.optDouble("spentAmount", groupObj.optDouble("spent_amount", 0));
                    int memberCount = groupObj.optInt("memberCount", groupObj.optInt("member_count", 1));
                    String createdAt = groupObj.optString("createdAt", groupObj.optString("created_at", ""));
                    int creatorId = groupObj.optInt("creatorId", groupObj.optInt("creator_id", 0));
                    String currency = groupObj.optString("currency", "USD");
                    String groupInviteCode = groupObj.optString("inviteCode", groupObj.optString("invite_code", ""));
                    String status = groupObj.optString("status", "active");

                    Group joinedGroup = new Group(
                            groupId,                    // int
                            name,                       // String
                            description,                // String
                            totalBudget,                // double
                            spentAmount,                // double
                            memberCount,                // int
                            createdAt,                  // String
                            creatorId,                  // int
                            currency,                   // String
                            groupInviteCode,            // String
                            status                      // String
                    );

                    if (listener != null) {
                        listener.onGroupJoined(joinedGroup);
                    }

                } catch (JSONException e) {
                    Log.e("GROUP_DEBUG", "JSON parse error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Error parsing response");
                    }
                }
            }
        });
    }

    // Get group details with members and expenses
    public void loadGroupDetails(String groupId) {
        String userIdStr = sharedPreferences.getString("userId", "");
        String baseUrl = "http://10.0.2.2/budgetpal_api/";

        if (userIdStr.isEmpty()) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        String url = baseUrl + "group_details.php?groupId=" + groupId + "&userId=" + userIdStr;
        Log.d("GROUP_DEBUG", "Loading group details from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GROUP_DEBUG", "Group details error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("GROUP_DEBUG", "Group details response: " + responseBody);

                // Parse and return group details
                // You'll need to implement this based on your group_details.php response
            }
        });
    }
}