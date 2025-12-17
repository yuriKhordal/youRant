package com.yurikh.yourant.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;

public class YouRantActivity extends AppCompatActivity {

    private ActionBar toolbar;
    private ImageButton btn_toolbar_search = null,
            btn_toolbar_refresh = null,
            btn_toolbar_login = null,
            btn_toolbar_profile = null;
    private CardView card_toolbar_profile = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbar = getSupportActionBar();
        if (toolbar != null) {
            if (!isTaskRoot()) {
                toolbar.setDisplayHomeAsUpEnabled(true);
            }

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.END | Gravity.CENTER_VERTICAL
            );
            View toolbarButtons = LayoutInflater.from(toolbar.getThemedContext())
                    .inflate(R.layout.actionbar_buttons, null);
            btn_toolbar_search = toolbarButtons.findViewById(R.id.btn_toolbar_search);
            btn_toolbar_refresh = toolbarButtons.findViewById(R.id.btn_toolbar_refresh);
            btn_toolbar_login = toolbarButtons.findViewById(R.id.btn_toolbar_login);
            btn_toolbar_profile = toolbarButtons.findViewById(R.id.btn_toolbar_profile);
            card_toolbar_profile = toolbarButtons.findViewById(R.id.card_toolbar_profile);

            toolbar.setDisplayShowCustomEnabled(true);
            toolbar.setDisplayShowTitleEnabled(true);
            toolbar.setCustomView(toolbarButtons, layoutParams);

            btn_toolbar_search.setOnClickListener(this::onBtnToolbarSearchClick);
            btn_toolbar_refresh.setOnClickListener(this::onBtnToolbarRefreshClick);
            btn_toolbar_login.setOnClickListener(this::onBtnToolbarLoginClick);
            btn_toolbar_profile.setClickable(true);
            btn_toolbar_profile.setOnClickListener(this::onBtnToolbarProfileClick);
        }

        hideToolbarSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (btn_toolbar_login != null && card_toolbar_profile != null && btn_toolbar_profile != null) {
            if (YouRantApp.getInstance().isLoggedIn()) {
                btn_toolbar_login.setVisibility(View.GONE);
                card_toolbar_profile.setVisibility(View.VISIBLE);
                btn_toolbar_profile.setBackgroundColor(Color.RED);
            } else {
                btn_toolbar_login.setVisibility(View.VISIBLE);
                card_toolbar_profile.setVisibility(View.GONE);
            }
        }

        YouRantApp.getInstance().checkForAuthFail(this);
    }

    protected void onBtnToolbarSearchClick(View view) {}
    protected void onBtnToolbarRefreshClick(View view) {}
    private void onBtnToolbarLoginClick(View view) {
        startActivity(LoginActivity.makeIntent(this));
    }
    private void onBtnToolbarProfileClick(View view) {
        Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
        
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    YouRantApp.getInstance().logout();
                    card_toolbar_profile.setVisibility(View.GONE);
                    btn_toolbar_login.setVisibility(View.VISIBLE);
                }).setNegativeButton("No", null)
                .show();
    }

    protected void hideToolbarSearch() {
        if (btn_toolbar_search != null) {
            btn_toolbar_search.setVisibility(View.GONE);
        }
    }

    protected void hideToolbarRefresh() {
        if (btn_toolbar_refresh != null) {
            btn_toolbar_refresh.setVisibility(View.GONE);
        }
    }

    protected void hideToolbarLogin() {
        if (btn_toolbar_login != null) {
            btn_toolbar_login.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
