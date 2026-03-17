package com.budgetpal.budgetppaal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AiAssistantDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate custom layout with WebView
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_assistant, null);
        WebView webView = dialogView.findViewById(R.id.ai_webview);

        // Configure WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Load your Chatbase chatbot
        webView.loadUrl("https://www.chatbase.co/chatbot-iframe/3yzxDLPREmwhHCvuRSLCU");

        builder.setView(dialogView)
                .setTitle("🤖 AI Budget Assistant")
                .setNegativeButton("Close", null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog full screen
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}