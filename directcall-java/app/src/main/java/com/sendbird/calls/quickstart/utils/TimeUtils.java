package com.sendbird.calls.quickstart.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String getTimeString(long periodMs) {
        final String result;
        int totalSec = (int)(periodMs / 1000);
        int hour = 0, min, sec;

        if (totalSec >= 3600) {
            hour = totalSec / 3600;
            totalSec = totalSec % 3600;
        }

        min = totalSec / 60;
        sec = totalSec % 60;

        if (hour > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);
        } else if (min > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d", min, sec);
        } else {
            result = String.format(Locale.getDefault(), "0:%02d", sec);
        }
        return result;
    }

    public static String getTimeStringForHistory(long periodMs) {
        final String result;
        int totalSec = (int)(periodMs / 1000);
        int hour = 0, min, sec;

        if (totalSec >= 3600) {
            hour = totalSec / 3600;
            totalSec = totalSec % 3600;
        }

        min = totalSec / 60;
        sec = totalSec % 60;

        if (hour > 0) {
            result = String.format(Locale.getDefault(), "%dh %dm %ds", hour, min, sec);
        } else if (min > 0) {
            result = String.format(Locale.getDefault(), "%dm %ds", min, sec);
        } else {
            result = String.format(Locale.getDefault(), "%ds", sec);
        }
        return result;
    }

    public static String getDateString(long timeMs) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy/MM/dd H:mm", Locale.getDefault());
        String dateString = simpleDateFormat.format(new Date(timeMs));
        return dateString.toLowerCase();
    }
}
