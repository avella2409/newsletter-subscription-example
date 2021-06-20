package com.avella.example.newslettersubscriptionexample.configuration;

import com.avella.example.newslettersubscriptionexample.application.query.GetAllSubscriberSubscriptionQuery;
import com.avella.example.newslettersubscriptionexample.application.query.handlers.PostgreGetAllSubscriberSubscriptionQueryHandler;
import com.avella.example.newslettersubscriptionexample.application.query.models.SubscriptionDTO;
import com.avella.example.newslettersubscriptionexample.application.query.shared.QueryHandler;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class QueryConfiguration {

    @Bean
    public QueryHandler<GetAllSubscriberSubscriptionQuery, Flux<SubscriptionDTO>>
    getAllSubscriberSubscriptionQueryHandler(PostgresqlConnectionFactory connectionFactory) {
        return new PostgreGetAllSubscriberSubscriptionQueryHandler(connectionFactory);
    }
}
