package com.avella.example.newslettersubscriptionexample.integration.clients.web;

import com.avella.example.newslettersubscriptionexample.application.query.GetAllSubscriberSubscriptionQuery;
import com.avella.example.newslettersubscriptionexample.application.query.models.SubscriptionDTO;
import com.avella.example.newslettersubscriptionexample.application.query.shared.QueryHandler;
import com.avella.example.newslettersubscriptionexample.clients.web.GetAllSubscriberSubscriptionController;
import com.avella.example.newslettersubscriptionexample.clients.web.models.SubscriptionJSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GetAllSubscriberSubscriptionController.class)
@Tag("integration")
class GetAllSubscriberSubscriptionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private QueryHandler<GetAllSubscriberSubscriptionQuery, Flux<SubscriptionDTO>> queryHandler;

    private final UUID subscriberId = UUID.randomUUID();
    private final String endpoint = "/subscriptions/" + subscriberId;

    @Test
    void returnAllSubscriberSubscription() {

        UUID newsletterId1 = UUID.randomUUID();
        UUID newsletterId2 = UUID.randomUUID();

        var queryHandlerInteraction = TestPublisher.<SubscriptionDTO>createCold().emit(
                new SubscriptionDTO(newsletterId1),
                new SubscriptionDTO(newsletterId2)
        );

        when(queryHandler.query(new GetAllSubscriberSubscriptionQuery(subscriberId)))
                .thenReturn(queryHandlerInteraction.flux());

        webTestClient.get().uri(endpoint)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SubscriptionJSON.class)
                .contains(
                        new SubscriptionJSON(newsletterId1),
                        new SubscriptionJSON(newsletterId2)
                );

        queryHandlerInteraction.assertWasSubscribed();
    }
}
