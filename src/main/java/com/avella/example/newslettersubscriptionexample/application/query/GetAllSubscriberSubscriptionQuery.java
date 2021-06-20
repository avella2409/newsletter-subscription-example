package com.avella.example.newslettersubscriptionexample.application.query;

import com.avella.example.newslettersubscriptionexample.application.query.shared.Query;

import java.util.UUID;

public record GetAllSubscriberSubscriptionQuery(UUID subscriberId) implements Query {
}
