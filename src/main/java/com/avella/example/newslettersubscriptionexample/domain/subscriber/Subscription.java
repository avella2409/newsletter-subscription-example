package com.avella.example.newslettersubscriptionexample.domain.subscriber;

import java.util.Comparator;
import java.util.UUID;

public record Subscription(UUID newsletterId, long subscriptionNumber) {
    public static Comparator<Subscription> sortBySubscriptionNumberAsc() {
        return (s1, s2) -> Long.compare(s1.subscriptionNumber, s2.subscriptionNumber());
    }
}
