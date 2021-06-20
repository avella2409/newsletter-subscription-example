package com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions;

import java.util.UUID;

public class AlreadyHasASubscriptionToTheNewsletterException extends RuntimeException{
    public AlreadyHasASubscriptionToTheNewsletterException(UUID newsletterId) {
        super("Already has a subscription to the newsletter " + newsletterId);
    }
}
