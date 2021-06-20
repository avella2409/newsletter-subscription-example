package com.avella.example.newslettersubscriptionexample.unit.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.InstantiateSubscriberCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.InstantiateSubscriberCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberAlreadyExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory.InMemorySubscriberRepository;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstantiateSubscriberCommandHandlerTest {

    private final InMemorySubscriberRepository allSubscriber = new InMemorySubscriberRepository();
    private final InstantiateSubscriberCommand command = new InstantiateSubscriberCommand(UUID.randomUUID(),
            "random@gmail.com");
    private final CommandHandler<InstantiateSubscriberCommand> handler =
            new InstantiateSubscriberCommandHandler(allSubscriber);

    @Test
    void instantiateSubscriberWhenHeIsNotAlreadyInstantiated() {
        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(1, numberOfSubscriber());
    }

    @Test
    void errorWhenSubscriberIsAlreadyInstantiated() {
        StepVerifier.create(handler.handle(command))
                .verifyComplete();
        StepVerifier.create(handler.handle(command))
                .verifyError(SubscriberAlreadyExistException.class);

        assertEquals(1, numberOfSubscriber());
    }

    @Test
    void subscriberHasFreePlanWhenInstantiated() {
        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertTrue(hasValidPlan(command.subscriberId(), SubscriberPlan.FREE));
    }

    @Test
    void subscriberHasNoSubscriptionWhenInstantiated() {
        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertTrue(hasNoSubscription(command.subscriberId()));
    }

    private boolean hasNoSubscription(UUID subscriberId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription().isEmpty();
    }

    private boolean hasValidPlan(UUID subscriberId, SubscriberPlan plan) {
        return allSubscriber.get(subscriberId).block().createMemento().plan() == plan;
    }

    private long numberOfSubscriber() {
        return allSubscriber.getAll().count().block();
    }

}
