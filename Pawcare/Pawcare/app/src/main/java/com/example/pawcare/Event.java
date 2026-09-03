package com.example.pawcare;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String title;
    private String timeRange;


    public Event(String title, String dateString, String timeString) {
        this.title = title;
        this.timeRange = timeRange;
    }

    public String getTitle() {
        return title;
    }


    public String getTimeRange() {
        return timeRange;
    }
}

