package com.senacor.memcachedui.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeFormatter {

    public static String getAgeFromSeconds(long totalSeconds) {
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        if (days > 0 ) {
            return String.format("%dd %dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }
}
