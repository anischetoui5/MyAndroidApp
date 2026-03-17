package com.budgetpal.budgetppaal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HomeActivity extends Fragment {

    private TextView tvWelcome;
    private LinearLayout chatContainer;
    private ScrollView scrollChat;
    private EditText inputMessage;
    private Button sendButton;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI
        tvWelcome = view.findViewById(R.id.tvWelcome);
        chatContainer = view.findViewById(R.id.chatContainer);
        scrollChat = view.findViewById(R.id.scrollChat);
        inputMessage = view.findViewById(R.id.inputMessage);
        sendButton = view.findViewById(R.id.sendButton);
        progressBar = view.findViewById(R.id.progressBar);

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Display welcome message
        String userName = sharedPreferences.getString("fullName", "User");
        String userEmail = sharedPreferences.getString("email", "");
        tvWelcome.setText("Welcome, " + userName + "!\nEmail: " + userEmail);

        // Send button listener
        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessageToChat("You: " + message);
                inputMessage.setText("");
                int userId = sharedPreferences.getInt("userId", 1); // Replace with actual userId
                new Thread(() -> sendToChatbot(userId, message)).start();
            }
        });

        return view;
    }

    private void addMessageToChat(String msg) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            TextView textView = new TextView(getContext());
            textView.setText(msg);
            textView.setPadding(10, 10, 10, 10);
            chatContainer.addView(textView);
            scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void sendToChatbot(int userId, String message) {
        try {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // Fetch user financial data
            URL url = new URL("http://10.84.27.20/budgetpal_api/get_finances.php?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            Scanner scanner = new Scanner(in).useDelimiter("\\A");
            String userData = scanner.hasNext() ? scanner.next() : "{}";
            scanner.close();
            conn.disconnect();

            // Prepare Chatbase payload
            JSONObject payload = new JSONObject();
            payload.put("message", message);
            payload.put("context", new JSONObject().put("financial_summary", new JSONObject(userData)));

            // Send to Chatbase API
            URL chatbaseUrl = new URL("https://api.chatbase.com/message"); // replace with your endpoint
            HttpURLConnection chatConn = (HttpURLConnection) chatbaseUrl.openConnection();
            chatConn.setRequestMethod("POST");
            chatConn.setRequestProperty("Authorization", "Bearer YOUR_API_KEY");
            chatConn.setRequestProperty("Content-Type", "application/json");
            chatConn.setDoOutput(true);

            OutputStream os = chatConn.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();

            InputStream chatIn = chatConn.getInputStream();
            Scanner chatScanner = new Scanner(chatIn).useDelimiter("\\A");
            String chatResponse = chatScanner.hasNext() ? chatScanner.next() : "{}";
            chatScanner.close();
            chatConn.disconnect();

            JSONObject chatJson = new JSONObject(chatResponse);
            String reply = chatJson.has("reply") ? chatJson.getString("reply") : "Sorry, I couldn't respond.";
            addMessageToChat("Bot: " + reply);

        } catch (Exception e) {
            addMessageToChat("Bot: Sorry, something went wrong.");
            e.printStackTrace();
        } finally {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        }
    }
}
