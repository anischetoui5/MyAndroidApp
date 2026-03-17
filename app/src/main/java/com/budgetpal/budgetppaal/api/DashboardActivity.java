package com.budgetpal.budgetppaal.api;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.budgetpal.budgetppaal.R;

import org.json.JSONObject;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout chatContainer;
    EditText inputMessage;
    ScrollView scrollChat;
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        chatContainer = findViewById(R.id.chatContainer);
        inputMessage = findViewById(R.id.inputMessage);
        scrollChat = findViewById(R.id.scrollChat);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString();
            if(!message.isEmpty()){
                addMessageToChat("You: " + message);
                inputMessage.setText("");
                new Thread(() -> sendToChatbot(1, message)).start(); // 1 = user_id
            }
        });
    }

    private void addMessageToChat(String msg){
        runOnUiThread(() -> {
            TextView textView = new TextView(this);
            textView.setText(msg);
            chatContainer.addView(textView);
            scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void sendToChatbot(int userId, String message){
        try {
            // 1️⃣ Get financial data from PHP API
            URL url = new URL("http://192.168.1.100/get_finances.php?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            Scanner scanner = new Scanner(in).useDelimiter("\\A");
            String userData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            // 2️⃣ Prepare JSON for Chatbase
            JSONObject payload = new JSONObject();
            payload.put("message", message);
            payload.put("context", new JSONObject().put("financial_summary", new JSONObject(userData)));

            // 3️⃣ Send to Chatbase API
            URL chatbaseUrl = new URL("https://api.chatbase.com/message"); // replace with actual endpoint
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
            String chatResponse = chatScanner.hasNext() ? chatScanner.next() : "";
            chatScanner.close();

            JSONObject chatJson = new JSONObject(chatResponse);
            String reply = chatJson.getString("reply"); // depends on Chatbase API structure

            addMessageToChat("Bot: " + reply);

        } catch (Exception e){
            addMessageToChat("Bot: Sorry, something went wrong.");
            e.printStackTrace();
        }
    }
}
