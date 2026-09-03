package com.example.pawcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomEventAdapter extends ArrayAdapter<Event> {
    private List<Event> events;

    public CustomEventAdapter(Context context, List<Event> events) {
        super(context, R.layout.item_event, events);
        this.events = events;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // Inflate the custom layout for each item
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
            holder = new ViewHolder();
            // Set up the ViewHolder to contain the correct views
            holder.title = convertView.findViewById(R.id.tvEventTitle);
            holder.timeRange = convertView.findViewById(R.id.tvEventTimeRange);
            // Store the ViewHolder in the convertView
            convertView.setTag(holder);
        } else {
            // Retrieve the ViewHolder from the convertView
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current event object based on the position
        Event event = getItem(position);

        // If the event object is not null, set the appropriate text in the TextViews
        if (event != null) {
            holder.title.setText(event.getTitle()); // Use getTitle or an equivalent method to get the title
            holder.timeRange.setText(event.getTimeRange()); // Use getTimeRange or an equivalent method to get the time range
        }

        // Return the completed view to be displayed
        return convertView;
    }

    // ViewHolder class to hold the views
    static class ViewHolder {
        TextView title;
        TextView timeRange;
    }
}