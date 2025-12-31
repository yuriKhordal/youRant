package com.yurikh.yourant.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.yurikh.yourant.model.DownvoteReason;
import com.yurikh.yourant.ui.BaseViewModel;

import java.util.function.Consumer;

public class RantVM extends BaseViewModel {
    final DownvoteReason[] REASONS = {DownvoteReason.NotForMe, DownvoteReason.Repost, DownvoteReason.OffensiveSpam};

    public RantVM(Context context) {
        super(context);
    }

    public AlertDialog.Builder downvoteAlert(Consumer<DownvoteReason> onDownvote) {
        Spinner spnReason = new Spinner(this.context, Spinner.MODE_DIALOG);
        spnReason.setAdapter(new ArrayAdapter<>(this.context, android.R.layout.simple_spinner_dropdown_item, REASONS));
        return new AlertDialog.Builder(this.context)
                .setTitle("Reason for downvote?")
                .setView(spnReason)
                .setPositiveButton("Downvote!", (di, i) ->
                        onDownvote.accept((DownvoteReason) spnReason.getSelectedItem())
                )
                .setNegativeButton("Cancel", null);
    }

    public AlertDialog.Builder downvoteAlert(Consumer<DownvoteReason> onDownvote, Runnable onDismiss) {
        Spinner spnReason = new Spinner(this.context, Spinner.MODE_DIALOG);
        spnReason.setAdapter(new ArrayAdapter<>(this.context, android.R.layout.simple_spinner_dropdown_item, REASONS));
        return new AlertDialog.Builder(this.context)
                .setTitle("Reason for downvote?")
                .setView(spnReason)
                .setPositiveButton("Downvote!", (di, i) ->
                        onDownvote.accept((DownvoteReason) spnReason.getSelectedItem())
                )
                .setNegativeButton("Cancel", (di, i) -> onDismiss.run())
                .setOnDismissListener(di -> onDismiss.run());
    }
}
