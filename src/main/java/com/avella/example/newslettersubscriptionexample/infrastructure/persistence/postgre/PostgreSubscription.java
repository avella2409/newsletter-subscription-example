package com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre;

import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PostgreSubscription(@JsonProperty("newsletterId") UUID newsletterId,
                                  @JsonProperty("subscriptionNumber") long subscriptionNumber) {

    public Subscription toSubscription() {
        return new Subscription(newsletterId, subscriptionNumber);
    }

    public static PostgreSubscription fromSubscription(Subscription subscription) {
        return new PostgreSubscription(subscription.newsletterId(), subscription.subscriptionNumber());
    }
}
