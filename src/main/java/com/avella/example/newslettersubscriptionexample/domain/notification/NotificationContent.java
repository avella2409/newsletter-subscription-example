package com.avella.example.newslettersubscriptionexample.domain.notification;

import java.time.LocalDateTime;

public record NotificationContent(String title, String body) {

    public static NotificationContent create(LocalDateTime currentTime, String title, String link) {
        if (isMorning(currentTime))
            return new MorningNotificationContentStrategy().notificationContent(title, link);
        return new AfternoonNotificationContentStrategy().notificationContent(title, link);
    }

    private static boolean isMorning(LocalDateTime currentTime) {
        return currentTime.getHour() < 12;
    }
}
