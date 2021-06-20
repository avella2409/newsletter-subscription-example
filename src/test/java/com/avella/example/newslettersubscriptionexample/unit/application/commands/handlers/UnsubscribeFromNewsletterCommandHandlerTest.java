package com.avella.example.newslettersubscriptionexample.unit.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.UnsubscribeFromNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.UnsubscribeFromNewsletterCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.NotSubscribedToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory.InMemorySubscriberRepository;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnsubscribeFromNewsletterCommandHandlerTest {

    private final SubscriberRepository allSubscriber = new InMemorySubscriberRepository();
    private final UnsubscribeFromNewsletterCommand command =
            new UnsubscribeFromNewsletterCommand(UUID.randomUUID(), UUID.randomUUID());
    private final CommandHandler<UnsubscribeFromNewsletterCommand> handler =
            new UnsubscribeFromNewsletterCommandHandler(allSubscriber);

    @Test
    void errorWhenSubscriberDoesntExist() {
        StepVerifier.create(handler.handle(command))
                .verifyError(SubscriberDoesntExistException.class);
    }

    @Test
    void errorWhenSubscriberHasNoSubscriptionToTheNewsletter() {
        createSubscriberWithFreePlan(command.subscriberId());

        StepVerifier.create(handler.handle(command))
                .verifyError(NotSubscribedToTheNewsletterException.class);
    }

    @Test
    void unsubscribeFromNewsletterWhenSubscriberIsSubscribedToTheNewsletter() {
        createSubscriberWithFreePlan(command.subscriberId());
        subscribeToAllNewsletter(command.subscriberId(),
                List.of(command.newsletterId(), UUID.randomUUID(), UUID.randomUUID()));

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(2, numberOfSubscription(command.subscriberId()));
        assertFalse(isSubscribedToNewsletter(command.subscriberId(), command.newsletterId()));
    }

    private boolean isSubscribedToNewsletter(UUID subscriberId, UUID newsletterId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription()
                .stream()
                .anyMatch(subscription -> subscription.newsletterId().equals(newsletterId));
    }

    private int numberOfSubscription(UUID subscriberId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription().size();
    }

    private void subscribeToAllNewsletter(UUID subscriberId, List<UUID> allNewsletter) {
        allNewsletter.forEach(newsletterId -> subscribeToNewsletter(subscriberId, newsletterId));
    }

    private void subscribeToNewsletter(UUID subscriberId, UUID newsletterId) {
        Subscriber subscriber = allSubscriber.get(subscriberId).block();
        subscriber.subscribeToNewsletter(newsletterId);
        allSubscriber.add(subscriber).block();
    }

    private void createSubscriberWithFreePlan(UUID subscriberId) {
        allSubscriber.add(new Subscriber(subscriberId, "random@gmail.com")).block();
    }
}
