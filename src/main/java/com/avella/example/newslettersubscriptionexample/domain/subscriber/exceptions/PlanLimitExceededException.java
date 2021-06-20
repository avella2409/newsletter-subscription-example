package com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions;

public class PlanLimitExceededException extends RuntimeException {

    public PlanLimitExceededException() {
        super("Plan limit exceeded");
    }
}
