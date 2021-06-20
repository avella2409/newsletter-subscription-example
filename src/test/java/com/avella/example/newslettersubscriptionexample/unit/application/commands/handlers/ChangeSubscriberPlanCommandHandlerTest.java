package com.avella.example.newslettersubscriptionexample.unit.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.ChangeSubscriberPlanCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.ChangeSubscriberPlanCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.InvalidPlanLevelException;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscription;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.ChangeToSamePlanException;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory.InMemorySubscriberRepository;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeSubscriberPlanCommandHandlerTest {

    private final SubscriberRepository allSubscriber = new InMemorySubscriberRepository();
    private final ChangeSubscriberPlanCommand changeToFreePlanCommand =
            new ChangeSubscriberPlanCommand(UUID.randomUUID(), 0);
    private final ChangeSubscriberPlanCommand changeToPremiumPlanCommand =
            new ChangeSubscriberPlanCommand(UUID.randomUUID(), 1);
    private final CommandHandler<ChangeSubscriberPlanCommand> handler =
            new ChangeSubscriberPlanCommandHandler(allSubscriber);

    @Test
    void errorWhenSubscriberDoesntExist() {
        StepVerifier.create(handler.handle(changeToPremiumPlanCommand))
                .verifyError(SubscriberDoesntExistException.class);
    }

    @Test
    void errorWhenPlanLevelIsUnknown() {
        StepVerifier.create(handler.handle(new ChangeSubscriberPlanCommand(UUID.randomUUID(), 2)))
                .verifyError(InvalidPlanLevelException.class);
    }

    @Test
    void upgradeToPremiumPlanWhenSubscriberHasFreePlanAndUpgrade() {
        createSubscriberWithFreePlan(changeToPremiumPlanCommand.subscriberId());

        StepVerifier.create(handler.handle(changeToPremiumPlanCommand))
                .verifyComplete();

        assertTrue(hasValidPlan(changeToPremiumPlanCommand.subscriberId(), SubscriberPlan.PREMIUM));
    }

    @Test
    void downgradeToFreePlanWhenSubscriberHasPremiumPlanAndDowngrade() {
        createSubscriberWithPremiumPlan(changeToFreePlanCommand.subscriberId());

        StepVerifier.create(handler.handle(changeToFreePlanCommand))
                .verifyComplete();

        assertTrue(hasValidPlan(changeToFreePlanCommand.subscriberId(), SubscriberPlan.FREE));
    }

    @Test
    void keepOnly5FirstSubscriptionWhenDowngradingToFreePlan() {
        createSubscriberWithPremiumPlan(changeToFreePlanCommand.subscriberId());

        List<UUID> allNewsletterId = createListOfRandomNewsletterId(10);
        subscribeToAllNewsletter(changeToFreePlanCommand.subscriberId(), allNewsletterId);

        StepVerifier.create(handler.handle(changeToFreePlanCommand))
                .verifyComplete();

        assertEquals(5, numberOfSubscription(changeToFreePlanCommand.subscriberId()));
        assertTrue(isSubscribedToAllNewsletter(changeToFreePlanCommand.subscriberId(), allNewsletterId.subList(0, 5)));
    }

    @Test
    void errorWhenSubscriberSwitchToSamePlan() {
        createSubscriberWithFreePlan(changeToFreePlanCommand.subscriberId());

        StepVerifier.create(handler.handle(changeToFreePlanCommand))
                .verifyError(ChangeToSamePlanException.class);

        assertTrue(hasValidPlan(changeToFreePlanCommand.subscriberId(), SubscriberPlan.FREE));
    }

    private int numberOfSubscription(UUID subscriberId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription().size();
    }

    private boolean isSubscribedToAllNewsletter(UUID subscriberId, List<UUID> allNewsletterId) {
        return allSubscriber.get(subscriberId).block().createMemento().allSubscription().stream()
                .map(Subscription::newsletterId)
                .toList()
                .containsAll(allNewsletterId);
    }

    private boolean hasValidPlan(UUID subscriberId, SubscriberPlan subscriberPlan) {
        return allSubscriber.get(subscriberId).block().createMemento().plan() == subscriberPlan;
    }

    private void subscribeToAllNewsletter(UUID subscriberId, List<UUID> allNewsletterId) {
        Subscriber subscriber = allSubscriber.get(subscriberId).block();
        allNewsletterId.forEach(subscriber::subscribeToNewsletter);
        allSubscriber.add(subscriber).block();
    }

    private List<UUID> createListOfRandomNewsletterId(int number) {
        return IntStream.range(0, number)
                .mapToObj(i -> UUID.randomUUID()).toList();
    }

    private void createSubscriberWithPremiumPlan(UUID subscriberId) {
        Subscriber subscriber = new Subscriber(subscriberId, "random@gmail.com");
        subscriber.changePlan(SubscriberPlan.PREMIUM);
        allSubscriber.add(subscriber).block();
    }

    private void createSubscriberWithFreePlan(UUID subscriberId) {
        allSubscriber.add(new Subscriber(subscriberId, "random@gmail.com")).block();
    }
}
