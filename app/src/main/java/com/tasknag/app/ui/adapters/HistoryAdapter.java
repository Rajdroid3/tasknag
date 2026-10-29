package com.tasknag.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tasknag.app.R;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<TaskOccurrence> items = new ArrayList<>();

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<TaskOccurrence> newItems) {
        items.clear();
        if (newItems != null) {
            for (TaskOccurrence occ : newItems) {
                if (occ.isTerminal()) {
                    items.add(occ);
                }
            }
        }
        // sort by completed/skipped epoch descending
        items.sort((a, b) -> {
            long aTime = a.getCompletedEpoch() != null ? a.getCompletedEpoch() : (a.getSkippedEpoch() != null ? a.getSkippedEpoch() : a.getScheduledEpoch());
            long bTime = b.getCompletedEpoch() != null ? b.getCompletedEpoch() : (b.getSkippedEpoch() != null ? b.getSkippedEpoch() : b.getScheduledEpoch());
            return Long.compare(bTime, aTime);
        });
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskOccurrence occ = items.get(position);
        holder.bind(occ);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatus;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvTimestamp;
        TextView tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }

        public void bind(TaskOccurrence occ) {
            Context ctx = itemView.getContext();
            tvTitle.setText(occ.getTaskTitle());
            tvCategory.setText(occ.getCategory());

            if (occ.getStatus() == OccurrenceStatus.COMPLETED) {
                ivStatus.setImageResource(R.drawable.ic_check_circle);
                ivStatus.setColorFilter(ContextCompat.getColor(ctx, R.color.status_completed_text));
                tvStatusBadge.setText("Completed");
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_completed_text));

                long compTime = occ.getCompletedEpoch() != null ? occ.getCompletedEpoch() : occ.getScheduledEpoch();
                tvTimestamp.setText(DateTimeUtils.formatEpochToFriendlyDate(compTime) + " at " + DateTimeUtils.formatEpochToTime(compTime));
            } else if (occ.getStatus() == OccurrenceStatus.SKIPPED) {
                ivStatus.setImageResource(R.drawable.ic_cancel);
                ivStatus.setColorFilter(ContextCompat.getColor(ctx, R.color.status_skipped_text));
                tvStatusBadge.setText("Skipped");
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_skipped_text));

                long skipTime = occ.getSkippedEpoch() != null ? occ.getSkippedEpoch() : occ.getScheduledEpoch();
                tvTimestamp.setText(DateTimeUtils.formatEpochToFriendlyDate(skipTime) + " at " + DateTimeUtils.formatEpochToTime(skipTime));
            } else {
                ivStatus.setImageResource(R.drawable.ic_warning);
                ivStatus.setColorFilter(ContextCompat.getColor(ctx, R.color.status_overdue_text));
                tvStatusBadge.setText("Missed");
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_overdue_text));
                tvTimestamp.setText(DateTimeUtils.formatEpochToFriendlyDate(occ.getScheduledEpoch()) + " at " + DateTimeUtils.formatEpochToTime(occ.getScheduledEpoch()));
            }
        }
    }
}
