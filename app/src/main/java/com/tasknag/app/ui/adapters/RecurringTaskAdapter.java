package com.tasknag.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tasknag.app.R;
import com.tasknag.app.model.Task;
import com.tasknag.app.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class RecurringTaskAdapter extends RecyclerView.Adapter<RecurringTaskAdapter.ViewHolder> {

    public interface OnRecurringActionListener {
        void onToggleEnabled(Task task, boolean isEnabled);
        void onEdit(Task task);
        void onDelete(Task task);
    }

    private final Context context;
    private final List<Task> tasks = new ArrayList<>();
    private final OnRecurringActionListener listener;

    public RecurringTaskAdapter(Context context, OnRecurringActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            for (Task t : newTasks) {
                if (t.isRecurring()) {
                    tasks.add(t);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View viewCategoryBar;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvSchedule;
        TextView tvNagSettings;
        TextView tvActiveDates;
        SwitchCompat switchEnabled;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            viewCategoryBar = itemView.findViewById(R.id.viewCategoryBar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvNagSettings = itemView.findViewById(R.id.tvNagSettings);
            tvActiveDates = itemView.findViewById(R.id.tvActiveDates);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Task task, OnRecurringActionListener listener) {
            tvTitle.setText(task.getTitle());
            tvCategory.setText(task.getCategory());
            tvSchedule.setText("🔄 " + task.getScheduleSummary());
            tvNagSettings.setText("⏰ " + task.getNagSummary());

            String dateRange = (task.getStartDate() != null ? task.getStartDate() : "Start")
                    + " → " + (task.getEndDate() != null ? task.getEndDate() : "Indefinite");
            tvActiveDates.setText("📅 " + dateRange);

            int color = ColorUtils.parseColor(task.getColorHex(), Color.parseColor("#1976D2"));
            viewCategoryBar.setBackgroundColor(color);

            switchEnabled.setOnCheckedChangeListener(null);
            switchEnabled.setChecked(task.isEnabled());
            cardView.setAlpha(task.isEnabled() ? 1.0f : 0.6f);

            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleEnabled(task, isChecked);
                }
                cardView.setAlpha(isChecked ? 1.0f : 0.6f);
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(task);
            });
        }
    }
}
