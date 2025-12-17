package com.yurikh.yourant.ui;

import android.content.Context;

public abstract class BaseViewModel {
    protected Context context;

    public BaseViewModel(Context context) {
        this.context = context;
    }
}
