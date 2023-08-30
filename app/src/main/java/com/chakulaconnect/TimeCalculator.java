package com.chakulaconnect;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeCalculator {
    public static String calculateTime(long joinDate) {
        Instant currentInstant = Instant.now();
        long currentTimestamp = currentInstant.toEpochMilli();

        String timeAgo;
        Duration duration = Duration.ofMillis(currentTimestamp - joinDate);

        if (duration.toMinutes() < 1) {
            timeAgo = "Just now";
        } else if (duration.toHours() < 1) {
            long minutes = duration.toMinutes();
            timeAgo = minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (duration.toDays() < 1) {
            long hours = duration.toHours();
            timeAgo = hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (duration.toDays() < 7) {
            long days = duration.toDays();
            timeAgo = days + " day" + (days != 1 ? "s" : "") + " ago";
        } else if (duration.toDays() < 365) {
            long weeks = duration.toDays() / 7;
            timeAgo = weeks + " week" + (weeks != 1 ? "s" : "") + " ago";
        } else {
            LocalDateTime storedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(joinDate), ZoneId.systemDefault());
            LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentInstant, ZoneId.systemDefault());

            long years = Duration.between(storedDateTime, currentDateTime).toDays() / 365;
            timeAgo = years + " year" + (years != 1 ? "s" : "") + " ago";
        }

        return timeAgo;
    }
}
