package com.avella.example.newslettersubscriptionexample.domain.subscriber;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriberRepository {
    Mono<Void> add(Subscriber subscriber);

    Mono<Subscriber> get(UUID subscriberId);

    Flux<Subscriber> getAllWithASubscriptionToTheNewsletter(UUID newsletterId);
}
