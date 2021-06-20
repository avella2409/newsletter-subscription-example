package com.avella.example.newslettersubscriptionexample.unit.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.SubscribeToNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.SubscribeToNewsletterCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.AlreadyHasASubscriptionToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.PlanLimitExceededException;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory.InMemorySubscriberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SubscribeToNewsletterCommandHandlerTest {

    private final SubscriberRepository allSubscriber = new InMemorySubscriberRepository();
    private final SubscribeToNewsletterCommand command =
            new SubscribeToNewsletterCommand(UUID.randomUUID(), UUID.randomUUID());
    private final CommandHandler<SubscribeToNewsletterCommand> handler =
            new SubscribeToNewsletterCommandHandler(allSubscriber);

    @Test
    void errorWhenSubscriberDoesntExist() {
        StepVerifier.create(handler.handle(command))
                .verifyError(SubscriberDoesntExistException.class);
    }

    @Test
    void errorWhenSubscriberHasAlreadyASubscriptionToTheNewsletter() {
        instantiateSubscriberWithFreePlan(command.subscriberId());

        StepVerifier.create(handler.handle(command))
                .verifyComplete();
        StepVerifier.create(handler.handle(command))
                .verifyError(AlreadyHasASubscriptionToTheNewsletterException.class);

        assertEquals(1, numberOfSubscription(command.subscriberId()));
    }

    @Nested
    class SubscriberHasFreePlan {

        SubscriberHasFreePlan() {
            instantiateSubscriberWithFreePlan(command.subscriberId());
        }

        @Test
        void subscribeToNewsletterWhenSubscriberIsBelowFreePlanSubscriptionThreshold() {
            subscribeToRandomNewsletter(command.subscriberId(),
                    SubscriberPlan.FREE.subscriptionThreshold - 1);

            StepVerifier.create(handler.handle(command))
                    .verifyComplete();

            assertTrue(hasSubscribedToNewsletter(command.subscriberId(), command.newsletterId()));
            assertEquals(SubscriberPlan.FREE.subscriptionThreshold, numberOfSubscription(command.subscriberId()));
        }

        @Test
        void errorWhenSubscriberIsAtFreePlanSubscriptionThreshold() {
            subscribeToRandomNewsletter(command.subscriberId(), SubscriberPlan.FREE.subscriptionThreshold);

            StepVerifier.create(handler.handle(command))
                    .verifyError(PlanLimitExceededException.class);

            assertFalse(hasSubscribedToNewsletter(command.subscriberId(), command.newsletterId()));
            assertEquals(SubscriberPlan.FREE.subscriptionThreshold, numberOfSubscription(command.subscriberId()));
        }
    }

    @Nested
    class SubscriberHasPremiumPlan {
        SubscriberHasPremiumPlan() {
            instantiateSubscriberWithPremiumPlan(command.subscriberId());
        }

        @Test
        void subscribeToNewsletterWhenSubscriberIsBelowPremiumPlanSubscriptionThreshold() {
            subscribeToRandomNewsletter(command.subscriberId(),
                    SubscriberPlan.PREMIUM.subscriptionThreshold - 1);

            StepVerifier.create(handler.handle(command))
                    .verifyComplete();

            assertTrue(hasSubscribedToNewsletter(command.subscriberId(), command.newsletterId()));
            assertEquals(SubscriberPlan.PREMIUM.subscriptionThreshold, numberOfSubscription(command.subscriberId()));
        }

        @Test
        void errorWhenSubscriberIsAtPremiumPlanSubscriptionThreshold() {
            subscribeToRandomNewsletter(command.subscriberId(), SubscriberPlan.PREMIUM.subscriptionThreshold);

            StepVerifier.create(handler.handle(command))
                    .verifyError(PlanLimitExceededException.class);

            assertFalse(hasSubscribedToNewsletter(command.subscriberId(), command.newsletterId()));
            assertEquals(SubscriberPlan.PREMIUM.subscriptionThreshold, numberOfSubscription(command.subscriberId()));
        }
    }

    private int numberOfSubscription(UUID subscriberId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription().size();
    }

    private void subscribeToRandomNewsletter(UUID subscriberId, int numberOfSubscription) {
        IntStream.range(0, numberOfSubscription)
                .forEach(i -> handler.handle(new SubscribeToNewsletterCommand(subscriberId, UUID.randomUUID())).block());
    }

    private boolean hasSubscribedToNewsletter(UUID subscriberId, UUID newsletterId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription()
                .stream().anyMatch(subscription -> subscription.newsletterId().equals(newsletterId));
    }

    private void instantiateSubscriberWithPremiumPlan(UUID subscriberId) {
        Subscriber subscriber = new Subscriber(subscriberId, "random@gmail.com");
        subscriber.changePlan(SubscriberPlan.PREMIUM);
        allSubscriber.add(subscriber).block();
    }

    private void instantiateSubscriberWithFreePlan(UUID subscriberId) {
        allSubscriber.add(new Subscriber(subscriberId, "random@gmail.com")).block();
    }
}
