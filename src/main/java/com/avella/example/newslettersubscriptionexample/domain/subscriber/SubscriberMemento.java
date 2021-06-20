package com.avella.example.newslettersubscriptionexample.domain.subscriber;

import com.avella.example.newslettersubscriptionexample.domain.shared.EntityMemento;

import java.util.Set;

public record SubscriberMemento(EntityMemento entityMemento, String email,
                                SubscriberPlan plan, Set<Subscription> allSubscription,
                                long lastSubscriptionNumber) {
}
