package com.budgetpal.budgetppaal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private FloatingActionButton fabAiAssistant;

    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private ChartsFragment chartsFragment;
    private GroupsFragment groupsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        initializeViews();
        setupFragments();
        setupBottomNavigation();
        setupAiAssistant();
        loadFragment(homeFragment);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);
        fabAiAssistant = findViewById(R.id.fab_ai_assistant);
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        chartsFragment = new ChartsFragment();
        groupsFragment = new GroupsFragment();
    }

    private void setupAiAssistant() {
        fabAiAssistant.setOnClickListener(v -> openChatbot());
    }

    private void openChatbot() {
        String url = "https://www.chatbase.co/chatbot-iframe/3yzxDLPREmwhHCvuRSLCU";

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build();

        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    loadFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    loadFragment(categoriesFragment);
                    return true;
                } else if (itemId == R.id.nav_charts) {
                    loadFragment(chartsFragment);
                    return true;
                } else if (itemId == R.id.nav_groups) {
                    loadFragment(groupsFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    showProfileMenu();
                    return true;
                }
                return false;
            }
        });
    }

    private void showProfileMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("fullName", "User");

        builder.setTitle("👤 " + userName + "'s Profile");
        builder.setItems(new String[]{"👤 View Profile", "⚙️ Settings", "🚪 Logout"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: showUserProfile(); break;
                            case 1: showSettings(); break;
                            case 2: logout(); break;
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "User");
        String email = prefs.getString("email", "No email");
        String currency = prefs.getString("currency", "USD");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Profile");
        builder.setMessage("Name: " + fullName + "\n" + "Email: " + email + "\n" + "Currency: " + currency);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showSettings() {
        Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performLogout();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void performLogout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(homeFragment);
        } else {
            super.onBackPressed();
        }
    }
}
