package com.avella.example.newslettersubscriptionexample.integration.clients.web;

import com.avella.example.newslettersubscriptionexample.application.commands.SubscribeToNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.web.SubscribeToNewsletterController;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.AlreadyHasASubscriptionToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.PlanLimitExceededException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.publisher.TestPublisher;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SubscribeToNewsletterController.class)
@Tag("integration")
class SubscribeToNewsletterControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommandHandler<SubscribeToNewsletterCommand> handler;

    private final UUID subscriberId = UUID.randomUUID();
    private final UUID newsletterId = UUID.randomUUID();
    private final String endpoint = "/subscribe/" + subscriberId + "/" + newsletterId;

    @Test
    void successWhenSubscriberExistAndNotAlreadySubscribedToTheNewsletter() {
        var handlerResponse = TestPublisher.<Void>createCold().complete();

        when(handler.handle(new SubscribeToNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.post().uri(endpoint)
                .exchange()
                .expectStatus().isOk();

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void notFoundStatusWhenSubscriberDoesntExist() {
        var handlerResponse = TestPublisher.<Void>createCold()
                .error(new SubscriberDoesntExistException(subscriberId));

        when(handler.handle(new SubscribeToNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.post().uri(endpoint)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void conflictStatusWhenSubscriberHasAlreadyASubscriptionToTheNewsletter() {
        var handlerResponse = TestPublisher.<Void>createCold()
                .error(new AlreadyHasASubscriptionToTheNewsletterException(newsletterId));

        when(handler.handle(new SubscribeToNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.post().uri(endpoint)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void forbiddenStatusWhenSubscriberExceedLimitOfHisPlan() {
        var handlerResponse = TestPublisher.<Void>createCold()
                .error(new PlanLimitExceededException());

        when(handler.handle(new SubscribeToNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.post().uri(endpoint)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);

        handlerResponse.assertWasSubscribed();
    }
}
