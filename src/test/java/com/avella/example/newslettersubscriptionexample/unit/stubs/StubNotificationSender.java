package com.avella.example.newslettersubscriptionexample.unit.stubs;

import com.avella.example.newslettersubscriptionexample.domain.notification.Notification;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationSender;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StubNotificationSender implements NotificationSender {

    private final Set<String> allEmailFailing = new HashSet<>();
    private final List<Notification> allNotificationSend = new LinkedList<>();

    @Override
    public Mono<Void> send(Notification notification) {
        return Mono.defer(() -> {
            if(allEmailFailing.contains(notification.recipient()))
                return Mono.error(new RuntimeException());
            allNotificationSend.add(notification);
            return Mono.empty();
        });
    }

    public boolean hasSendNotificationToEmail(String email) {
        return allNotificationSend.stream().anyMatch(notification -> notification.recipient().equals(email));
    }

    public boolean hasNotSendAnyNotification() {
        return allNotificationSend.isEmpty();
    }

    public int numberOfNotificationSend() {
        return allNotificationSend.size();
    }

    public String firstNotificationBody() {
        return allNotificationSend.get(0).notificationContent().body();
    }

    public String firstNotificationTitle() {
        return allNotificationSend.get(0).notificationContent().title();
    }

    public void failWithEmail(String email) {
        allEmailFailing.add(email);
    }
}
