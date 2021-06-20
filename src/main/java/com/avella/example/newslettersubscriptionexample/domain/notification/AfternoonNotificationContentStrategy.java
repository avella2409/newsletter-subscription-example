package com.avella.example.newslettersubscriptionexample.domain.notification;

public class AfternoonNotificationContentStrategy implements NotificationContentStrategy {

    @Override
    public NotificationContent notificationContent(String title, String link) {
        return new NotificationContent(title, "Time to relax ! Take a break and appreciate the brand new newsletter named \""
                + title + "\" by clicking on this link " + link);
    }
}
