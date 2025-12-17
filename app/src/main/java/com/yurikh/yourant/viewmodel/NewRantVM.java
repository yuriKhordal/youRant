package com.yurikh.yourant.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;

import com.yurikh.yourant.model.RantCategory;
import com.yurikh.yourant.ui.BaseViewModel;
import com.yurikh.yourant.ui.NewRantActivity;

import kotlin.Pair;
import kotlin.Triple;

public class NewRantVM extends BaseViewModel {
    public static final String SAVE_STATE_PREF = "rant_draft";
    public static final String SAVE_STATE_RANT = "rant_content";
    public static final String SAVE_STATE_TAGS = "rant_tags";
    public static final String SAVE_STATE_TYPE = "rant_type";

    public static final RantCategory[] RANT_TYPES = {
            RantCategory.Rant, RantCategory.JokeMeme, RantCategory.Question,
            RantCategory.devRant, RantCategory.Random
    };

    public NewRantVM(Context context) {
        super(context);
    }

    public ArrayAdapter<RantCategory>  getRantTypesAdapter() {
        return new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, RANT_TYPES);
    }

    public int getRantTypeSelectionPosition(RantCategory type) {
        if (type == RantCategory.Rant)
            return 0;
        else if (type == RantCategory.JokeMeme)
            return 1;
        else if (type == RantCategory.Question)
            return 2;
        else if (type == RantCategory.devRant)
            return 3;
        else if (type == RantCategory.Random)
            return 4;

        throw new IllegalArgumentException("Error: Unexpected RantCategory '" + type + "'.");
    }

    public void saveDraftToPrefs(String content, String tags, RantCategory type) {
        context.getSharedPreferences(SAVE_STATE_PREF, Context.MODE_PRIVATE).edit()
                .putString(SAVE_STATE_RANT, content)
                .putString(SAVE_STATE_TAGS, tags)
                .putInt(SAVE_STATE_TYPE, type.value)
                .apply();
    }

    public Triple<String, String, RantCategory> loadDraftFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(SAVE_STATE_PREF, Context.MODE_PRIVATE);
        String rant = prefs.getString(SAVE_STATE_RANT, null);
        String tags = prefs.getString(SAVE_STATE_TAGS, null);
        int type = prefs.getInt(SAVE_STATE_TYPE, -1);
        RantCategory category = (type >= 0) ? RantCategory.fromValue(type) : null;
        return new Triple<>(rant, tags, category);
    }
}
