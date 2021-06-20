package com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions;

public class ChangeToSamePlanException extends RuntimeException{

    public ChangeToSamePlanException() {
        super("Change to same plan");
    }
}
