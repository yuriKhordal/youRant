package com.yurikh.yourant.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.yourant.Helper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.AuthToken;
import com.yurikh.yourant.network.API;

public class LoginActivity extends YouRantActivity {

    EditText txt_username, txt_password;
    ProgressBar throbber;

    public static Intent makeIntent(Context ctx) {
        return new Intent(ctx, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txt_username = findViewById(R.id.txt_username);
        txt_password = findViewById(R.id.txt_password);
        throbber = findViewById(R.id.throbber);

        hideToolbarRefresh();
        hideToolbarSearch();
        hideToolbarLogin();
    }

    public void onBtnLoginClick(View view) {
        final String username = txt_username.getText().toString();
        final String password = txt_password.getText().toString();
        final Toast toast = Toast.makeText(this, "Logged in successfully", Toast.LENGTH_LONG);

        throbber.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                AuthToken token = API.postAuth(username, password);
                YouRantApp.getInstance().login(token);
                runOnUiThread(() -> {
                    toast.show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Helper.displayError(this, e));
            } finally {
                runOnUiThread(() -> throbber.setVisibility(View.GONE));
            }
        }).start();
    }
}