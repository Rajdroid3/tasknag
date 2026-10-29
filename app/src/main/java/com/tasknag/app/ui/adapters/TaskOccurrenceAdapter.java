package com.tasknag.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.tasknag.app.R;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.util.ColorUtils;
import com.tasknag.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskOccurrenceAdapter extends RecyclerView.Adapter<TaskOccurrenceAdapter.ViewHolder> {

    public interface OnOccurrenceActionListener {
        void onComplete(TaskOccurrence occurrence);
        void onSnooze(TaskOccurrence occurrence, int snoozeMinutes);
        void onSkip(TaskOccurrence occurrence);
        void onItemClick(TaskOccurrence occurrence);
    }

    private final Context context;
    private final List<TaskOccurrence> items = new ArrayList<>();
    private final OnOccurrenceActionListener listener;

    public TaskOccurrenceAdapter(Context context, OnOccurrenceActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<TaskOccurrence> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_occurrence, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskOccurrence occ = items.get(position);
        holder.bind(occ, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View viewCategoryBar;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvTime;
        TextView tvStatusPill;
        TextView tvNagBadge;
        MaterialButton btnComplete;
        MaterialButton btnSnooze;
        MaterialButton btnMore;
        ImageView ivStatusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            viewCategoryBar = itemView.findViewById(R.id.viewCategoryBar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatusPill = itemView.findViewById(R.id.tvStatusPill);
            tvNagBadge = itemView.findViewById(R.id.tvNagBadge);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnSnooze = itemView.findViewById(R.id.btnSnooze);
            btnMore = itemView.findViewById(R.id.btnMore);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
        }

        public void bind(TaskOccurrence occ, OnOccurrenceActionListener listener) {
            Context ctx = itemView.getContext();
            tvTitle.setText(occ.getTaskTitle());
            tvCategory.setText(occ.getCategory());
            tvTime.setText(occ.getFormattedTime());

            int categoryColor = ColorUtils.parseColor(occ.getColorHex(), Color.parseColor("#1976D2"));
            viewCategoryBar.setBackgroundColor(categoryColor);

            long now = System.currentTimeMillis();
            OccurrenceStatus status = occ.getStatus();

            // Status pill styling
            if (status == OccurrenceStatus.COMPLETED) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.6f);
                tvStatusPill.setText("✓ Completed " + occ.getFormattedCompletedTime());
                tvStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.status_completed_text));
                tvStatusPill.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_completed_bg));
                btnComplete.setVisibility(View.GONE);
                btnSnooze.setVisibility(View.GONE);
                tvNagBadge.setVisibility(View.GONE);
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.status_completed_text));
            } else if (status == OccurrenceStatus.SKIPPED) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
                tvStatusPill.setText("✕ Skipped");
                tvStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.status_skipped_text));
                tvStatusPill.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_skipped_bg));
                btnComplete.setVisibility(View.GONE);
                btnSnooze.setVisibility(View.GONE);
                tvNagBadge.setVisibility(View.GONE);
                ivStatusIcon.setImageResource(R.drawable.ic_cancel);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.status_skipped_text));
            } else if (status == OccurrenceStatus.OVERDUE || occ.isOverdueNow(now)) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
                long overdueMin = occ.getOverdueMinutes(now);
                tvStatusPill.setText("⚠️ Overdue by " + overdueMin + "m");
                tvStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.status_overdue_text));
                tvStatusPill.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_overdue_bg));
                btnComplete.setVisibility(View.VISIBLE);
                btnSnooze.setVisibility(View.VISIBLE);
                ivStatusIcon.setImageResource(R.drawable.ic_warning);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.status_overdue_text));

                if (occ.getNagCount() > 0) {
                    tvNagBadge.setVisibility(View.VISIBLE);
                    tvNagBadge.setText("Nag #" + occ.getNagCount());
                    tvNagBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_overdue_text));
                    tvNagBadge.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_overdue_bg));
                } else {
                    tvNagBadge.setVisibility(View.GONE);
                }
            } else if (status == OccurrenceStatus.DUE_NOW || occ.isDueOrOverdue(now)) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
                tvStatusPill.setText("⚡ Due Now");
                tvStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.status_due_text));
                tvStatusPill.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_due_bg));
                btnComplete.setVisibility(View.VISIBLE);
                btnSnooze.setVisibility(View.VISIBLE);
                ivStatusIcon.setImageResource(R.drawable.ic_alarm);
                ivStatusIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.status_due_text));

                if (occ.getNagCount() > 0) {
                    tvNagBadge.setVisibility(View.VISIBLE);
                    tvNagBadge.setText("Nag #" + occ.getNagCount());
                    tvNagBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_due_text));
                    tvNagBadge.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_due_bg));
                } else {
                    tvNagBadge.setVisibility(View.GONE);
                }
            } else {
                // Upcoming
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
                tvStatusPill.setText("Upcoming • " + occ.getFormattedTime());
                tvStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.status_upcoming_text));
                tvStatusPill.setBackgroundColor(ContextCompat.getColor(ctx, R.color.status_upcoming_bg));
                btnComplete.setVisibility(View.VISIBLE);
                btnSnooze.setVisibility(View.GONE);
                tvNagBadge.setVisibility(View.GONE);
                ivStatusIcon.setImageResource(R.drawable.ic_schedule);
                ivStatusIcon.setColorFilter(Color.parseColor("#1565C0"));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(occ);
            });

            // Complete button
            btnComplete.setOnClickListener(v -> {
                if (listener != null) listener.onComplete(occ);
            });

            // Snooze button with quick dropdown
            btnSnooze.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(ctx, btnSnooze);
                popup.getMenu().add(0, 5, 0, "⏰ Snooze 5 minutes");
                popup.getMenu().add(0, 10, 1, "⏰ Snooze 10 minutes");
                popup.getMenu().add(0, 15, 2, "⏰ Snooze 15 minutes");
                popup.getMenu().add(0, 30, 3, "⏰ Snooze 30 minutes");
                popup.getMenu().add(0, 60, 4, "⏰ Snooze 1 hour");
                popup.setOnMenuItemClickListener(item -> {
                    if (listener != null) {
                        listener.onSnooze(occ, item.getItemId());
                    }
                    return true;
                });
                popup.show();
            });

            // More button (Skip, etc.)
            btnMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(ctx, btnMore);
                if (!occ.isTerminal()) {
                    popup.getMenu().add(0, 1, 0, "✕ Mark as Skipped");
                }
                popup.getMenu().add(0, 2, 1, "ℹ️ View Details");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1 && listener != null) {
                        listener.onSkip(occ);
                    } else if (item.getItemId() == 2 && listener != null) {
                        listener.onItemClick(occ);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }
}
