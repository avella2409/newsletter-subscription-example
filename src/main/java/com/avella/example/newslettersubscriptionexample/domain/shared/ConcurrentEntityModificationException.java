package com.avella.example.newslettersubscriptionexample.domain.shared;

import java.util.UUID;

public class ConcurrentEntityModificationException extends RuntimeException{

    public ConcurrentEntityModificationException(UUID entityId) {
        super("Concurrent entity modification " + entityId);
    }
}
