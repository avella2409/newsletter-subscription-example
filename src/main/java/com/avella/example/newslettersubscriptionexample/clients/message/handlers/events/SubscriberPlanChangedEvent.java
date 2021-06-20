package com.avella.example.newslettersubscriptionexample.clients.message.handlers.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SubscriberPlanChangedEvent(@JsonProperty("eventId") UUID eventId,
                                         @JsonProperty("subscriberId") UUID subscriberId,
                                         @JsonProperty("planLevel") int planLevel) {
}
