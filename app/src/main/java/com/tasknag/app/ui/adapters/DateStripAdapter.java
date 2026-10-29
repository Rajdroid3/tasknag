package com.tasknag.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tasknag.app.R;
import com.tasknag.app.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateStripAdapter extends RecyclerView.Adapter<DateStripAdapter.ViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(String dateStr);
    }

    public static class DayItem {
        public String dateStr; // YYYY-MM-DD
        public String dayOfWeek; // Mon, Tue
        public String dayNumber; // 28
        public boolean isToday;

        public DayItem(String dateStr, String dayOfWeek, String dayNumber, boolean isToday) {
            this.dateStr = dateStr;
            this.dayOfWeek = dayOfWeek;
            this.dayNumber = dayNumber;
            this.isToday = isToday;
        }
    }

    private final Context context;
    private final List<DayItem> days = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnDateSelectedListener listener;

    public DateStripAdapter(Context context, OnDateSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        generateDays();
    }

    private void generateDays() {
        days.clear();
        Calendar cal = Calendar.getInstance();
        // Start 2 days before today up to 12 days in the future (total 15 days)
        cal.add(Calendar.DAY_OF_YEAR, -2);

        SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumFmt = new SimpleDateFormat("d", Locale.getDefault());

        String todayStr = DateTimeUtils.getTodayDateString();

        for (int i = 0; i < 15; i++) {
            String dateStr = DateTimeUtils.formatEpochToDate(cal.getTimeInMillis());
            String dayName = dayNameFmt.format(cal.getTime());
            String dayNum = dayNumFmt.format(cal.getTime());
            boolean isToday = dateStr.equals(todayStr);

            if (isToday) {
                selectedPosition = i;
            }

            days.add(new DayItem(dateStr, dayName, dayNum, isToday));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public String getSelectedDate() {
        if (selectedPosition >= 0 && selectedPosition < days.size()) {
            return days.get(selectedPosition).dateStr;
        }
        return DateTimeUtils.getTodayDateString();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date_strip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayItem item = days.get(position);
        boolean isSelected = (position == selectedPosition);
        holder.bind(item, isSelected);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDateSelected(item.dateStr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDayOfWeek;
        TextView tvDayNumber;
        View viewDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardDate);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            viewDot = itemView.findViewById(R.id.viewDot);
        }

        public void bind(DayItem item, boolean isSelected) {
            tvDayOfWeek.setText(item.dayOfWeek);
            tvDayNumber.setText(item.dayNumber);

            if (isSelected) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                tvDayOfWeek.setTextColor(Color.WHITE);
                tvDayNumber.setTextColor(Color.WHITE);
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.date_item_bg));
                tvDayOfWeek.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.date_item_subtext));
                tvDayNumber.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.date_item_text));
            }

            viewDot.setVisibility(item.isToday ? View.VISIBLE : View.INVISIBLE);
            viewDot.setBackgroundColor(isSelected ? Color.WHITE : Color.parseColor("#1976D2"));
        }
    }
}
