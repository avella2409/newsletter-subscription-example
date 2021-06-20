package com.avella.example.newslettersubscriptionexample.domain.notification;

public interface NotificationContentStrategy {

    NotificationContent notificationContent(String title, String link);
}
