package org.example.bycicon;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Search_Engine {

    public static String PostedAt(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(createdAt, now);

        if (seconds < 60) {
            return "Just now";
        }

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) {
            return hours + " hours ago";
        }

        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days < 7) {
            return days + " days ago";
        }

        long weeks = days / 7;
        if (days < 30) {
            return weeks + " weeks ago";
        }

        long months = days / 30;
        if (days < 365) {
            return months + " months ago";
        }

        long years = days / 365;
        return years + " years ago";
    }
}