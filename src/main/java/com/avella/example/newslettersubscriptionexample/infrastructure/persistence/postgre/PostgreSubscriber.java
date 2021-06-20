package com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre;

import com.avella.example.newslettersubscriptionexample.domain.shared.EntityMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record PostgreSubscriber(EntityMemento entityMemento, String email,
                                int planLevel, String allSubscriptionJson,
                                long lastSubscriptionNumber) {

    public Subscriber toSubscriber(ObjectMapper objectMapper) {
        SubscriberMemento memento = new SubscriberMemento(entityMemento, email,
                planLevelToSubscriberPlan(),
                jsonToSetOfSubscription(objectMapper),
                lastSubscriptionNumber);
        return Subscriber.fromMemento(memento);
    }

    public static PostgreSubscriber fromSubscriber(Subscriber subscriber, ObjectMapper objectMapper) {
        SubscriberMemento memento = subscriber.createMemento();

        return new PostgreSubscriber(memento.entityMemento(), memento.email(),
                subscriberPlanToPlanLevel(memento.plan()),
                setOfSubscriptionToJson(memento.allSubscription(), objectMapper),
                memento.lastSubscriptionNumber());
    }

    private static String setOfSubscriptionToJson(Set<Subscription> allSubscription, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(allSubscription.stream()
                    .map(PostgreSubscription::fromSubscription).toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Subscription> jsonToSetOfSubscription(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(allSubscriptionJson, new TypeReference<List<PostgreSubscription>>() {
            })
                    .stream().map(PostgreSubscription::toSubscription).collect(Collectors.toSet());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private SubscriberPlan planLevelToSubscriberPlan() {
        if (planLevel == 0)
            return SubscriberPlan.FREE;
        return SubscriberPlan.PREMIUM;
    }

    private static int subscriberPlanToPlanLevel(SubscriberPlan subscriberPlan) {
        if (subscriberPlan == SubscriberPlan.FREE)
            return 0;
        return 1;
    }
}
