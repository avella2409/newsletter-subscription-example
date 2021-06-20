package com.avella.example.newslettersubscriptionexample.unit.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.NotifySubscriberOfNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.NotifySubscriberOfNewsletterCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory.InMemorySubscriberRepository;
import com.avella.example.newslettersubscriptionexample.unit.stubs.StubNotificationSender;
import com.avella.example.newslettersubscriptionexample.unit.stubs.StubTimeService;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifySubscriberOfNewsletterCommandHandlerTest {

    private final StubTimeService stubTimeService = new StubTimeService();
    private final StubNotificationSender stubNotificationSender = new StubNotificationSender();
    private final SubscriberRepository allSubscriber = new InMemorySubscriberRepository();
    private final NotifySubscriberOfNewsletterCommand command =
            new NotifySubscriberOfNewsletterCommand(UUID.randomUUID(), "Random Title",
                    "https://www.random-link.com");
    private final CommandHandler<NotifySubscriberOfNewsletterCommand> handler =
            new NotifySubscriberOfNewsletterCommandHandler(allSubscriber, stubNotificationSender, stubTimeService);

    @Test
    void dontSendAnyNotificationWhenThereIsNoSubscriberForTheNewsletter() {
        StepVerifier.create(handler.handle(command))
                .verifyComplete();
        assertTrue(stubNotificationSender.hasNotSendAnyNotification());
    }

    @Test
    void sendOneNotificationWhenThereIsOneSubscriberForTheNewsletter() {
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId());

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(1, stubNotificationSender.numberOfNotificationSend());
    }

    @Test
    void sendNotificationForEverySubscriberOfTheNewsletter() {
        IntStream.range(0, 20)
                .forEach(i -> createSubscriberWithASubscriptionToNewsletter(command.newsletterId()));

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(20, stubNotificationSender.numberOfNotificationSend());
    }

    @Test
    void sendNotificationToValidEmail() {
        String email1 = "xyz@gmail.com";
        String email2 = "abc@gmail.com";
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId(), email1);
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId(), email2);

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(2, stubNotificationSender.numberOfNotificationSend());
        assertTrue(stubNotificationSender.hasSendNotificationToEmail(email1));
        assertTrue(stubNotificationSender.hasSendNotificationToEmail(email2));
    }

    @Test
    void sendNotificationWithValidMessageWhenItsBefore12AM() {
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId());
        stubTimeService.withFixedTime(LocalDateTime.of(2021, 1, 14, 11, 59, 59));

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(1, stubNotificationSender.numberOfNotificationSend());
        assertEquals("Random Title", stubNotificationSender.firstNotificationTitle());
        assertEquals("Hurry up ! Come read the brand new " +
                        "newsletter named \"Random Title\" by clicking on this link https://www.random-link.com",
                stubNotificationSender.firstNotificationBody());
    }

    @Test
    void sendNotificationWithValidMessageWhenItsAfterOrEqualTo12AM() {
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId());
        stubTimeService.withFixedTime(LocalDateTime.of(2021, 1, 14, 12, 0, 0));

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(1, stubNotificationSender.numberOfNotificationSend());
        assertEquals("Random Title", stubNotificationSender.firstNotificationTitle());
        assertEquals("Time to relax ! Take a break and " +
                "appreciate the brand new newsletter named \"Random Title\" " +
                "by clicking on this link https://www.random-link.com", stubNotificationSender.firstNotificationBody());
    }

    @Test
    void continueToSendNotificationEvenWhenOneNotificationFail() {
        String email1 = "xyz@gmail.com";
        String email2 = "abc@gmail.com";
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId(), email1);
        createSubscriberWithASubscriptionToNewsletter(command.newsletterId(), email2);

        stubNotificationSender.failWithEmail(email1);

        StepVerifier.create(handler.handle(command))
                .verifyComplete();

        assertEquals(1, stubNotificationSender.numberOfNotificationSend());
        assertTrue(stubNotificationSender.hasSendNotificationToEmail(email2));
    }

    private void createSubscriberWithASubscriptionToNewsletter(UUID newsletterId) {
        createSubscriberWithASubscriptionToNewsletter(newsletterId, "random@gmail.com");
    }

    private void createSubscriberWithASubscriptionToNewsletter(UUID newsletterId, String email) {
        Subscriber subscriber = new Subscriber(UUID.randomUUID(), email);
        subscriber.subscribeToNewsletter(newsletterId);
        allSubscriber.add(subscriber).block();
    }
}
