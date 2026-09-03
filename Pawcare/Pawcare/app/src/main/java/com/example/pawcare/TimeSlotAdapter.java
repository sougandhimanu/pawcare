package com.example.pawcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private ArrayList<String> timeSlots;
    private int selectedItem = -1;
    private OnTimeSlotClickListener timeSlotClickListener;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(String timeSlot, int position);
    }

    public TimeSlotAdapter(ArrayList<String> timeSlots, OnTimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.timeSlotClickListener = listener;
    }

    @Override
    public TimeSlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeslot_item, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
        String timeSlot = timeSlots.get(position);
        holder.timeSlotTextView.setText(timeSlot);

        holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),
                selectedItem == position ? R.drawable.time_slot_background_selected : R.drawable.time_slot_background));

        holder.itemView.setOnClickListener(v -> {
            int previousItem = selectedItem;
            selectedItem = position;
            notifyItemChanged(previousItem); // Only notify the item that gets deselected
            notifyItemChanged(position); // Only notify the item that gets selected
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotClick(timeSlot, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView timeSlotTextView;

        TimeSlotViewHolder(View itemView) {
            super(itemView);
            timeSlotTextView = itemView.findViewById(R.id.tvTimeSlot);
        }
    }

    // Additional method to get the selected time slot
    public String getSelectedTimeSlot() {
        return selectedItem >= 0 ? timeSlots.get(selectedItem) : null;
    }
}
