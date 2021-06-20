package com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions;

public class InvalidPlanLevelException extends RuntimeException{

    public InvalidPlanLevelException(int planLevel) {
        super("Invalid plan level " + planLevel);
    }
}
