package com.avella.example.newslettersubscriptionexample.domain.shared;

import java.util.UUID;

public class EntityIdAlreadyExistException extends RuntimeException {

    public EntityIdAlreadyExistException(UUID id) {
        super("Entity ID already exist " + id);
    }
}
