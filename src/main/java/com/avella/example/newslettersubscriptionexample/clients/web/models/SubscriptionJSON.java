package com.avella.example.newslettersubscriptionexample.clients.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SubscriptionJSON(@JsonProperty("newsletterId") UUID newsletterId) {
}
