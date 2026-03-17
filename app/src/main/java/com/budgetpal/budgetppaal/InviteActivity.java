package com.budgetpal.budgetppaal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class InviteActivity extends AppCompatActivity {

    private TextView tvInviteCode, tvGroupName;
    private Button btnCopyCode, btnShareLink, btnGenerateNew;
    private EditText etInviteEmail;
    private Button btnInviteEmail;

    private String groupId;
    private String groupName;
    private String inviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        // Get group data from intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        groupName = intent.getStringExtra("groupName");

        // Initialize views
        tvGroupName = findViewById(R.id.tvGroupName);
        tvInviteCode = findViewById(R.id.tvInviteCode);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        btnShareLink = findViewById(R.id.btnShareLink);
        btnGenerateNew = findViewById(R.id.btnGenerateNew);
        etInviteEmail = findViewById(R.id.etInviteEmail);
        btnInviteEmail = findViewById(R.id.btnInviteEmail);

        // Set group name
        tvGroupName.setText("Invite Members to: " + groupName);

        // Generate initial invite code
        generateInviteCode();

        // Setup click listeners
        setupClickListeners();
    }

    private void generateInviteCode() {
        // Generate a unique invite code (you can make this more sophisticated)
        inviteCode = "BP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        tvInviteCode.setText("Invite Code: " + inviteCode);

        // Save to database (you'll implement this with your PHP backend)
        // saveInviteCodeToDatabase();
    }

    private void setupClickListeners() {
        // Copy invite code to clipboard
        btnCopyCode.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", inviteCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Invite code copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        // Share invite link
        btnShareLink.setOnClickListener(v -> {
            String inviteLink = "https://budgetpal.app/join/" + inviteCode;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my BudgetPal group!");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Join my budget group '" + groupName + "' on BudgetPal!\n" +
                            "Use invite code: " + inviteCode + "\n" +
                            "Or click: " + inviteLink);
            startActivity(Intent.createChooser(shareIntent, "Share invite"));
        });

        // Generate new invite code
        btnGenerateNew.setOnClickListener(v -> {
            generateInviteCode();
            Toast.makeText(this, "New invite code generated!", Toast.LENGTH_SHORT).show();
        });

        // Invite via email
        btnInviteEmail.setOnClickListener(v -> {
            String email = etInviteEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send email invitation (you'll implement this with your PHP backend)
            sendEmailInvitation(email);
            etInviteEmail.setText("");
        });
    }

    private void sendEmailInvitation(String email) {
        // TODO: Implement API call to send email invitation
        // This should call your PHP backend to send the invitation email

        // For now, show a success message
        Toast.makeText(this, "Invitation sent to " + email, Toast.LENGTH_SHORT).show();
    }
}