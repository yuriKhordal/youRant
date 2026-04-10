package com.yurikh.yourant.ui;

import android.content.Context;

public abstract class BaseViewModel {
    protected final Context context;

    public BaseViewModel(Context context) {
        this.context = context;
    }
}
