package com.zpedroo.voltzauctions.utils.config;

import com.zpedroo.voltzauctions.utils.FileUtils;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static com.zpedroo.voltzauctions.utils.config.Settings.DATE_FORMAT;
import static com.zpedroo.voltzauctions.utils.config.TimeFormatter.Translations.*;

public class TimeFormatter {

    public static String millisToFormattedDate(long timeInMillis) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

        return dateFormatter.format(timeInMillis);
    }

    public static String millisToFormattedTime(long timeInMillis) {
        StringBuilder builder = new StringBuilder();

        Long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis) - (TimeUnit.MILLISECONDS.toDays(timeInMillis) * 24);
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - (TimeUnit.MILLISECONDS.toHours(timeInMillis) * 60);
        Long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - (TimeUnit.MILLISECONDS.toMinutes(timeInMillis) * 60);

        if (hours > 0) builder.append(hours).append(" ").append(hours == 1 ? HOUR : HOURS).append(" ");
        if (minutes > 0) builder.append(minutes).append(" ").append(minutes == 1 ? MINUTE : MINUTES).append(" ");
        if (seconds > 0) builder.append(seconds).append(" ").append(seconds == 1 ? SECOND : SECONDS);

        return builder.toString().isEmpty() ? NOW : builder.toString();
    }

    static class Translations {

        public static final String SECOND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.second"));

        public static final String SECONDS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.seconds"));

        public static final String MINUTE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minute"));

        public static final String MINUTES = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minutes"));

        public static final String HOUR = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hour"));

        public static final String HOURS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hours"));

        public static final String NOW = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.now"));

        private static String getColored(String str) {
            return ChatColor.translateAlternateColorCodes('&', str);
        }
    }
}