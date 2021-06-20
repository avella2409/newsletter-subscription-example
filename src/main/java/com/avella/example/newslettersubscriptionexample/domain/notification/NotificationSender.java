package com.avella.example.newslettersubscriptionexample.domain.notification;

import reactor.core.publisher.Mono;

public interface NotificationSender {

    Mono<Void> send(Notification notification);
}
