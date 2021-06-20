package com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions;

import java.util.UUID;

public class NotSubscribedToTheNewsletterException extends RuntimeException {

    public NotSubscribedToTheNewsletterException(UUID newsletterId) {
        super("Not subscribed to the newsletter " + newsletterId);
    }
}
