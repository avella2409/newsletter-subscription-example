package com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions;

import java.util.UUID;

public class SubscriberDoesntExistException extends RuntimeException {

    public SubscriberDoesntExistException(UUID subscriberId) {
        super("Subscriber doesnt exist " + subscriberId);
    }
}
