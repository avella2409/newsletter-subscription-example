package com.avella.example.newslettersubscriptionexample.domain.notification;

public class MorningNotificationContentStrategy implements NotificationContentStrategy {

    @Override
    public NotificationContent notificationContent(String title, String link) {
        return new NotificationContent(title, "Hurry up ! Come read the brand new newsletter named \"" + title
                + "\" by clicking on this link " + link);
    }
}
