package gsk.onetcore;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis -= TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis -= TimeUnit.MINUTES.toMillis(minutes));
        StringBuilder sb = new StringBuilder(64);
        if (hours != 0) {
            sb.append(hours);
            sb.append(" hours");
        }
        if (minutes != 0) {
            sb.append(String.valueOf(hours != 0 ? ", " : "") + minutes);
            sb.append(" minutes");
        }
        if (seconds != 0) {
            sb.append(String.valueOf(minutes != 0 ? ", " : "") + seconds);
            sb.append(" seconds");
        }
        return sb.toString();
    }

    public static String getConvertedTime(long i) {
        i = Math.abs(i);
        int hours = (int)Math.floor(i / 3600);
        int remainder = (int)(i % 3600);
        int minutes = remainder / 60;
        int seconds = remainder % 60;
        if (seconds == 0 && minutes == 0) {
            return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + "0 seconds";
        }
        if (minutes == 0) {
            if (seconds == 1) {
                return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%s seconds", seconds);
            }
            return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%s seconds", seconds);
        }
        if (seconds == 0) {
            if (minutes == 1) {
                return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%sm", minutes);
            }
            return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%sm", minutes);
        }
        if (seconds == 1) {
            if (minutes == 1) {
                return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%sm %ss", minutes, seconds);
            }
            return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%sm %ss", minutes, seconds);
        }
        if (minutes == 1) {
            return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + String.format("%sm %ss", minutes, seconds);
        }
        String toReturn = String.format("%sm %ss", minutes, seconds);
        return String.valueOf(hours != 0 ? (hours == 1 ? new StringBuilder(String.valueOf(hours)).append("h").toString() : new StringBuilder(String.valueOf(hours)).append("h").toString()) : "") + " " + toReturn;
    }

    public static String getMMSS(int seconds) {
        int millis = seconds * 1000;
        int sec = millis / 1000 % 60;
        int min = millis / 60000 % 60;
        int hr = millis / 3600000 % 24;
        return String.valueOf(hr > 0 ? String.format("%02d", hr) : "") + String.format("%02d:%02d", min, sec);
    }
}
