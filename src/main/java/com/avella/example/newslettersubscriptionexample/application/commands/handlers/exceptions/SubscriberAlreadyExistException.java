package com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions;

import java.util.UUID;

public class SubscriberAlreadyExistException extends RuntimeException {

    public SubscriberAlreadyExistException(UUID subscriberId) {
        super("Subscriber already exist " + subscriberId);
    }
}
