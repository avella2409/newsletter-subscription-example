package com.avella.example.newslettersubscriptionexample.application.query.handlers;

import com.avella.example.newslettersubscriptionexample.application.query.GetAllSubscriberSubscriptionQuery;
import com.avella.example.newslettersubscriptionexample.application.query.models.SubscriptionDTO;
import com.avella.example.newslettersubscriptionexample.application.query.shared.QueryHandler;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PostgreGetAllSubscriberSubscriptionQueryHandler
        implements QueryHandler<GetAllSubscriberSubscriptionQuery, Flux<SubscriptionDTO>> {

    private final Mono<PostgresqlConnection> connection;

    public PostgreGetAllSubscriberSubscriptionQueryHandler(PostgresqlConnectionFactory connectionFactory) {
        connection = connectionFactory.create();
    }

    @Override
    public Flux<SubscriptionDTO> query(GetAllSubscriberSubscriptionQuery query) {
        return connection.flatMapMany(co -> co.createStatement("""
                SELECT json_array_elements(allSubscription) ->> 'newsletterId' AS newsletterId 
                FROM subscriber 
                WHERE id=$1
                """)
                .bind("$1", query.subscriberId())
                .execute()
                .flatMap(result -> result.map((row, rowMetadata) ->
                        new SubscriptionDTO(UUID.fromString(row.get("newsletterId", String.class)))))
                .doFinally(signalType -> co.close().subscribe())
        );
    }
}
