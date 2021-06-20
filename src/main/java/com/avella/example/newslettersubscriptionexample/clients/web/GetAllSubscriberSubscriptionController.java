package com.avella.example.newslettersubscriptionexample.clients.web;

import com.avella.example.newslettersubscriptionexample.application.query.GetAllSubscriberSubscriptionQuery;
import com.avella.example.newslettersubscriptionexample.application.query.models.SubscriptionDTO;
import com.avella.example.newslettersubscriptionexample.application.query.shared.QueryHandler;
import com.avella.example.newslettersubscriptionexample.clients.web.models.SubscriptionJSON;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
public class GetAllSubscriberSubscriptionController {

    private final QueryHandler<GetAllSubscriberSubscriptionQuery, Flux<SubscriptionDTO>> queryHandler;

    public GetAllSubscriberSubscriptionController(QueryHandler<GetAllSubscriberSubscriptionQuery,
            Flux<SubscriptionDTO>> queryHandler) {
        this.queryHandler = queryHandler;
    }

    @GetMapping("/subscriptions/{subscriberId}")
    public Flux<SubscriptionJSON> getAllSubscriberSubscription(@PathVariable UUID subscriberId) {
        return queryHandler.query(new GetAllSubscriberSubscriptionQuery(subscriberId))
                .map(info -> new SubscriptionJSON(info.newsletterId()));
    }
}
