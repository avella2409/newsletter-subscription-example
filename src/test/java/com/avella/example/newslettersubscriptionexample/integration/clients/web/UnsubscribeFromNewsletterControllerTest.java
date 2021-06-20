package com.avella.example.newslettersubscriptionexample.integration.clients.web;

import com.avella.example.newslettersubscriptionexample.application.commands.UnsubscribeFromNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.web.UnsubscribeFromNewsletterController;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.NotSubscribedToTheNewsletterException;
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

@WebFluxTest(controllers = UnsubscribeFromNewsletterController.class)
@Tag("integration")
class UnsubscribeFromNewsletterControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommandHandler<UnsubscribeFromNewsletterCommand> handler;

    private final UUID subscriberId = UUID.randomUUID();
    private final UUID newsletterId = UUID.randomUUID();
    private final String endpoint = "/subscribe/" + subscriberId + "/" + newsletterId;

    @Test
    void successWhenSubscriberExistAndHasASubscriptionToNewsletter() {
        var handlerResponse = TestPublisher.<Void>createCold().complete();

        when(handler.handle(new UnsubscribeFromNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.delete().uri(endpoint)
                .exchange()
                .expectStatus().isOk();

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void notFoundStatusWhenSubscriberDoesntExist() {
        var handlerResponse = TestPublisher.<Void>createCold()
                .error(new SubscriberDoesntExistException(subscriberId));

        when(handler.handle(new UnsubscribeFromNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.delete().uri(endpoint)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void conflictStatusWhenSubscriberHasNoSubscriptionToNewsletter() {
        var handlerResponse = TestPublisher.<Void>createCold()
                .error(new NotSubscribedToTheNewsletterException(newsletterId));

        when(handler.handle(new UnsubscribeFromNewsletterCommand(subscriberId, newsletterId)))
                .thenReturn(handlerResponse.mono());

        webTestClient.delete().uri(endpoint)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        handlerResponse.assertWasSubscribed();
    }
}
