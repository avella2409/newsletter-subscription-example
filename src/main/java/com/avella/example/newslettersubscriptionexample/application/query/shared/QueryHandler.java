package com.avella.example.newslettersubscriptionexample.application.query.shared;

public interface QueryHandler<T extends Query, R> {
    R query(T query);
}
