package com.avella.example.newslettersubscriptionexample.domain.subscriber;

public enum SubscriberPlan {
    FREE(5),
    PREMIUM(30);

    public final int subscriptionThreshold;

    SubscriberPlan(int subscriptionThreshold) {
        this.subscriptionThreshold = subscriptionThreshold;
    }
}
