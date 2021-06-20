package com.avella.example.newslettersubscriptionexample.clients.message.handlers.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record NewsletterPublishedEvent(@JsonProperty("eventId") UUID eventId,
                                       @JsonProperty("newsletterId") UUID newsletterId,
                                       @JsonProperty("title") String title,
                                       @JsonProperty("link") String link) {
}
