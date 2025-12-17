package com.yurikh.yourant.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.yourant.Helper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.model.RantCategory;
import com.yurikh.yourant.network.API;
import com.yurikh.yourant.viewmodel.NewRantVM;

public class NewRantActivity extends AppCompatActivity {
    private static final String SAVE_STATE_RANT = NewRantVM.SAVE_STATE_RANT;
    private static final String SAVE_STATE_TAGS = NewRantVM.SAVE_STATE_TAGS;
    private static final String SAVE_STATE_TYPE = NewRantVM.SAVE_STATE_TYPE;

    NewRantVM vm;
    Spinner spn_rant_type;
    EditText txt_content, txt_tags;
    TextView lbl_content_len;
    Button btn_post;
    ProgressBar throbber;

    public static Intent makeIntent(Context ctx) {
        return new Intent(ctx, NewRantActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_rant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vm = new NewRantVM(this);
        spn_rant_type = findViewById(R.id.spn_rant_type);
        txt_content = findViewById(R.id.txt_content);
        lbl_content_len = findViewById(R.id.lbl_content_len);
        txt_tags = findViewById(R.id.txt_tags);
        throbber = findViewById(R.id.throbber);
        btn_post = findViewById(R.id.btn_post);

        spn_rant_type.setAdapter(vm.getRantTypesAdapter());
        spn_rant_type.setSelection(0);
        spn_rant_type.setOnItemSelectedListener(onSpnRantTypeSelect);

        lbl_content_len.setText("0 / 5000");
        txt_content.addTextChangedListener(txtContentTextWatcher);
    }

    @Override
    protected void onPause() {
        super.onPause();

        final String content = txt_content.getText().toString();
        final String tags = txt_tags.getText().toString();
        final RantCategory type = (RantCategory) spn_rant_type.getSelectedItem();
        vm.saveDraftToPrefs(content, tags, type);
    }

    @Override
    protected void onResume() {
        super.onResume();

        var draft = vm.loadDraftFromPrefs();
        if (draft.getFirst() != null)
            txt_content.setText(draft.getFirst());
        if (draft.getSecond() != null)
            txt_tags.setText(draft.getSecond());
        if (draft.getThird() != null)
            spn_rant_type.setSelection(vm.getRantTypeSelectionPosition(draft.getThird()));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        final String content = txt_content.getText().toString();
        final String tags = txt_tags.getText().toString();
        final int selection = spn_rant_type.getSelectedItemPosition();

        outState.putString(SAVE_STATE_RANT, content);
        outState.putString(SAVE_STATE_TAGS, tags);
        outState.putInt(SAVE_STATE_TYPE, selection);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final String content = savedInstanceState.getString(SAVE_STATE_RANT);
        final String tags = savedInstanceState.getString(SAVE_STATE_TAGS);
        final int selection = savedInstanceState.getInt(SAVE_STATE_TYPE, 0);

        txt_content.setText(content);
        txt_tags.setText(tags);
        spn_rant_type.setSelection(selection);
    }

    public void onBtnPostClick(View view) {
        final String content = txt_content.getText().toString();
        final String tags = txt_tags.getText().toString();
        final RantCategory type = (RantCategory) spn_rant_type.getSelectedItem();
        
        if (content.length() < 6 || content.length() > 5000) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Rant must be between 6 and 5000 characters!")
                    .setPositiveButton("Ok", null)
                    .show();
            return;
        }

        throbber.setVisibility(View.VISIBLE);
        btn_post.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                long rant_id = API.postRant(type, content, tags);
                runOnUiThread(() -> {
                    Intent intent = RantActivity.makeIntent(this, rant_id);
                    finish();
                    startActivity(intent);
                });
            } catch (Exception ex) {
                runOnUiThread(() -> Helper.displayError(this, ex));
            } finally {
                runOnUiThread(() -> {
                    throbber.setVisibility(View.GONE);
                    btn_post.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private final AdapterView.OnItemSelectedListener onSpnRantTypeSelect = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            RantCategory type = (RantCategory) adapterView.getSelectedItem();
            if (type == RantCategory.Rant) {
                txt_content.setHint(R.string.new_rant_rant_hint);
            } else if (type == RantCategory.JokeMeme) {
                txt_content.setHint(R.string.new_rant_joke_hint);
            } else if (type == RantCategory.Question) {
                txt_content.setHint(R.string.new_rant_question_hint);
            } else if (type == RantCategory.devRant) {
                txt_content.setHint(R.string.new_rant_devrant_hint);
            } else if (type == RantCategory.Random) {
                txt_content.setHint(R.string.new_rant_random_hint);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    };
    private final TextWatcher txtContentTextWatcher = new TextWatcher() {
        @Override public void afterTextChanged(Editable editable) {}
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String len = charSequence.length() + " / 5000";
            lbl_content_len.setText(len);
        }
    };
}