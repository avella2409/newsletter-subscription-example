package com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre;

import com.avella.example.newslettersubscriptionexample.domain.shared.EntityMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class PostgreSubscriberMapper implements BiFunction<Row, RowMetadata, Subscriber> {

    private final ObjectMapper objectMapper;

    public PostgreSubscriberMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Subscriber apply(Row row, RowMetadata rowMetadata) {
        Long surrogateId = row.get("surrogateId", Long.class);
        LocalDateTime lastUpdateTime = row.get("lastUpdateTime", LocalDateTime.class);
        LocalDateTime creationTime = row.get("creationTime", LocalDateTime.class);
        Long version = row.get("version", Long.class);
        UUID id = row.get("id", UUID.class);
        String email = row.get("email", String.class);
        Integer plan = row.get("plan", Integer.class);
        String allSubscriptionJson = row.get("allSubscription", String.class);
        Long lastSubscriptionNumber = row.get("lastSubscriptionNumber", Long.class);

        EntityMemento entityMemento = new EntityMemento(surrogateId, lastUpdateTime, creationTime, version, id);
        PostgreSubscriber postgreSubscriber = new PostgreSubscriber(entityMemento, email, plan,
                allSubscriptionJson, lastSubscriptionNumber);

        return postgreSubscriber.toSubscriber(objectMapper);
    }
}
