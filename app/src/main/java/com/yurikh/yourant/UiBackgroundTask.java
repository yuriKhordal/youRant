package com.yurikh.yourant;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UiBackgroundTask<T> implements DefaultLifecycleObserver {
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private final LifecycleOwner lifecycleOwner;
    private Supplier<T> backTask;
    private Consumer<T> uiTask;
    public boolean running;
    List<Consumer<Throwable>> errorHandlers;
    Set<Class<? extends Throwable>> errorTypes;

    public UiBackgroundTask(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        this.backTask = null;
        this.uiTask = null;
        this.running = false;
        this.errorHandlers = new ArrayList<>();
        this.errorTypes = new HashSet<>();

        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public UiBackgroundTask<T> task(Supplier<T> backTask){
        if (this.backTask != null) {
            throw new IllegalStateException("Background task is already set.");
        }

        this.backTask = backTask;
        return this;
    }

    public UiBackgroundTask<T> then(Consumer<T> uiTask) {
        if (this.backTask != null) {
            throw new IllegalStateException("UI task is already set.");
        }

        this.uiTask = uiTask;
        return this;
    }

    public <E extends Throwable> UiBackgroundTask<T> ifError(Class<E> error, Consumer<E> handler) {
        Consumer<Throwable> wrapper = (ex) -> {
            if (error.isInstance(ex)) {
                handler.accept(error.cast(ex));
            }
        };
        errorHandlers.add(wrapper);
        errorTypes.add(error);
        return this;
    }

    public void start() {
        if (running)
            throw new IllegalStateException("Task already started.");
        if (backTask == null)
            throw new IllegalStateException("No background task set.");
        if (isDestroyed())
            return;

        running = true;
        new Thread(() -> {
            try {
                T result = backTask.get();
                if (uiTask == null)
                    return;
                mainThread.post(() -> {
                    if (!isDestroyed())
                        uiTask.accept(result);
                });
            } catch (Throwable ex) {
                mainThread.post(() -> {
                    if (isDestroyed())
                        return;
                    if (errorTypes.stream().noneMatch(eClass -> eClass.isInstance(ex)))
                        throw ex;
                    errorHandlers.forEach(handler -> handler.accept(ex));
                });
            }
        }).start();
    }

    public boolean isDestroyed() {
        return lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED;
    }
}
