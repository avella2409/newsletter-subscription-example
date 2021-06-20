package com.avella.example.newslettersubscriptionexample.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.InstantiateSubscriberCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.shared.EntityIdAlreadyExistException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberAlreadyExistException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Mono;

public class InstantiateSubscriberCommandHandler implements CommandHandler<InstantiateSubscriberCommand> {

    private final SubscriberRepository allSubscriber;

    public InstantiateSubscriberCommandHandler(SubscriberRepository allSubscriber) {
        this.allSubscriber = allSubscriber;
    }

    @Override
    public Mono<Void> handle(InstantiateSubscriberCommand command) {
        return allSubscriber.add(new Subscriber(command.subscriberId(), command.email()))
                .onErrorMap(EntityIdAlreadyExistException.class,
                        err -> new SubscriberAlreadyExistException(command.subscriberId()));
    }
}
