package com.avella.example.newslettersubscriptionexample.integration.application.query;

import com.avella.example.newslettersubscriptionexample.application.query.GetAllSubscriberSubscriptionQuery;
import com.avella.example.newslettersubscriptionexample.application.query.handlers.PostgreGetAllSubscriberSubscriptionQueryHandler;
import com.avella.example.newslettersubscriptionexample.application.query.models.SubscriptionDTO;
import com.avella.example.newslettersubscriptionexample.application.query.shared.QueryHandler;
import com.avella.example.newslettersubscriptionexample.configuration.postgre.PostgreConfiguration;
import com.avella.example.newslettersubscriptionexample.configuration.postgre.PostgreInfo;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre.PostgreSubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Testcontainers
@Tag("integration")
class PostgreGetAllSubscriberSubscriptionQueryHandlerTest {

    @Container
    private static PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres"));
    private final Random random = new Random();
    private PostgreSubscriberRepository allSubscriber;
    private QueryHandler<GetAllSubscriberSubscriptionQuery, Flux<SubscriptionDTO>> queryHandler;

    @BeforeEach
    void init() {
        PostgresqlConnectionFactory connectionFactory = new PostgreConfiguration()
                .connectionFactory(new PostgreInfo(container.getHost(),
                        Integer.parseInt(container.getJdbcUrl().split("localhost:")[1].split("/")[0]),
                        container.getUsername(),
                        container.getPassword(),
                        container.getDatabaseName()));
        allSubscriber = new PostgreSubscriberRepository(connectionFactory, new ObjectMapper());
        queryHandler = new PostgreGetAllSubscriberSubscriptionQueryHandler(connectionFactory);

        deleteTable();
        createTable();
    }

    @Test
    void returnZeroSubscriptionWhenSubscriberDoesntExist() {
        StepVerifier.create(queryHandler.query(new GetAllSubscriberSubscriptionQuery(UUID.randomUUID())))
                .verifyComplete();
    }

    @Test
    void returnZeroSubscriptionWhenSubscriberExistButHaveZeroSubscription() {
        Subscriber subscriber = createRandomSubscriber();
        saveSubscriber(subscriber);

        StepVerifier.create(queryHandler.query(new GetAllSubscriberSubscriptionQuery(subscriber.id())))
                .verifyComplete();
    }

    @Test
    void returnAllSubscriptionOfSubscriber() {
        UUID newsletterId1 = UUID.randomUUID();
        UUID newsletterId2 = UUID.randomUUID();

        Subscriber subscriber = createRandomSubscriber();
        subscriber.subscribeToNewsletter(newsletterId1);
        subscriber.subscribeToNewsletter(newsletterId2);

        saveSubscriber(subscriber);

        StepVerifier.create(queryHandler.query(new GetAllSubscriberSubscriptionQuery(subscriber.id())))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(t -> true)
                .expectRecordedMatches(recorded -> recorded.size() == 2 && recorded.containsAll(List.of(
                        new SubscriptionDTO(newsletterId1),
                        new SubscriptionDTO(newsletterId2)
                )))
                .verifyComplete();
    }

    @Test
    void returnOnlySubscriptionOfSpecifiedSubscriber() {
        UUID newsletterId1 = UUID.randomUUID();
        UUID newsletterId2 = UUID.randomUUID();

        Subscriber subscriber1 = createRandomSubscriber();
        Subscriber subscriber2 = createRandomSubscriber();
        Subscriber subscriber3 = createRandomSubscriber();

        subscriber1.subscribeToNewsletter(newsletterId1);
        subscriber1.subscribeToNewsletter(newsletterId2);
        subscriber2.subscribeToNewsletter(UUID.randomUUID());
        subscriber3.subscribeToNewsletter(UUID.randomUUID());

        saveAllSubscriber(List.of(subscriber1, subscriber2, subscriber3));

        StepVerifier.create(queryHandler.query(new GetAllSubscriberSubscriptionQuery(subscriber1.id())))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(t -> true)
                .expectRecordedMatches(recorded -> recorded.size() == 2 && recorded.containsAll(List.of(
                        new SubscriptionDTO(newsletterId1),
                        new SubscriptionDTO(newsletterId2)
                )))
                .verifyComplete();
    }

    private void saveAllSubscriber(List<Subscriber> allSubscriberToSave) {
        allSubscriberToSave.forEach(this::saveSubscriber);
    }

    private void saveSubscriber(Subscriber subscriber) {
        allSubscriber.add(subscriber).block();
    }

    private Subscriber createRandomSubscriber() {
        return new Subscriber(UUID.randomUUID(), "random" + random.nextInt() + "@gmail.com");
    }

    private void deleteTable() {
        try {
            allSubscriber.deleteTable().block();
        } catch (Exception e) {
            System.out.println("Table already deleted");
        }
    }

    private void createTable() {
        try {
            allSubscriber.createTable().block();
        } catch (Exception e) {
            System.out.println("Table already created");
        }
    }

}
