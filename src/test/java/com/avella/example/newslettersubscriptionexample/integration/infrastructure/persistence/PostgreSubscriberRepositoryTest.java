package com.avella.example.newslettersubscriptionexample.integration.infrastructure.persistence;

import com.avella.example.newslettersubscriptionexample.configuration.postgre.PostgreConfiguration;
import com.avella.example.newslettersubscriptionexample.configuration.postgre.PostgreInfo;
import com.avella.example.newslettersubscriptionexample.domain.shared.Entity;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre.PostgreSubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@Tag("integration")
class PostgreSubscriberRepositoryTest {

    @Container
    private static PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres"));
    private final Random random = new Random();
    private PostgreSubscriberRepository allSubscriber;

    @BeforeEach
    void init() {
        allSubscriber = new PostgreSubscriberRepository(new PostgreConfiguration()
                .connectionFactory(new PostgreInfo(container.getHost(),
                        Integer.parseInt(container.getJdbcUrl().split("localhost:")[1].split("/")[0]),
                        container.getUsername(),
                        container.getPassword(),
                        container.getDatabaseName())),
                new ObjectMapper());
        deleteTable();
        createTable();
    }

    @Test
    void save() {
        List<Subscriber> allSubscriberToSave = List.of(
                createRandomSubscriber(),
                createRandomSubscriber(),
                createRandomSubscriber()
        );

        saveAllSubscriber(allSubscriberToSave);

        assertEquals(3, numberOfSubscriber());
        assertTrue(allSubscriberAreSavedWithValidValue(allSubscriberToSave));
    }

    @Test
    void update() {
        Subscriber subscriber = createRandomSubscriber();
        saveSubscriber(subscriber);

        Subscriber savedSubscriber = allSubscriber.get(subscriber.id()).block();
        savedSubscriber.changePlan(SubscriberPlan.PREMIUM);
        savedSubscriber.subscribeToNewsletter(UUID.randomUUID());
        savedSubscriber.subscribeToNewsletter(UUID.randomUUID());

        StepVerifier.create(allSubscriber.add(savedSubscriber))
                .verifyComplete();

        assertEquals(1, numberOfSubscriber());
        assertTrue(subscriberIsSavedWithValidValue(savedSubscriber));
    }

    @Test
    void getAllWithASubscriptionToNewsletter() {
        UUID newsLetterId = UUID.randomUUID();
        Subscriber subscriber1 = createRandomSubscriber();
        Subscriber subscriber2 = createRandomSubscriber();
        Subscriber subscriber3 = createRandomSubscriber();

        subscriber1.subscribeToNewsletter(UUID.randomUUID());
        subscriber2.subscribeToNewsletter(UUID.randomUUID());
        subscriber2.subscribeToNewsletter(newsLetterId);
        subscriber3.subscribeToNewsletter(newsLetterId);
        subscriber3.subscribeToNewsletter(UUID.randomUUID());

        saveAllSubscriber(List.of(subscriber1, subscriber2, subscriber3));

        StepVerifier.create(allSubscriber.getAllWithASubscriptionToTheNewsletter(newsLetterId))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(t -> true)
                .expectRecordedMatches(recorded -> recorded.size() == 2
                        && recorded.stream().map(Entity::id).toList()
                        .containsAll(List.of(subscriber2.id(), subscriber3.id())))
                .verifyComplete();
    }

    private void saveAllSubscriber(List<Subscriber> allSubscriberToSave) {
        allSubscriberToSave.forEach(this::saveSubscriber);
    }

    private void saveSubscriber(Subscriber subscriber) {
        StepVerifier.create(allSubscriber.add(subscriber))
                .verifyComplete();
    }

    private boolean isEqual(Subscriber expected, Subscriber actual) {
        SubscriberMemento expectedMemento = expected.createMemento();
        SubscriberMemento actualMemento = actual.createMemento();

        return expectedMemento.entityMemento().id().equals(actualMemento.entityMemento().id())
                && expectedMemento.email().equals(actualMemento.email())
                && expectedMemento.allSubscription().equals(actualMemento.allSubscription())
                && expectedMemento.lastSubscriptionNumber() == actualMemento.lastSubscriptionNumber();
    }

    private boolean allSubscriberAreSavedWithValidValue(List<Subscriber> allSubscriberToSave) {
        return allSubscriberToSave.stream()
                .filter(this::subscriberIsSavedWithValidValue)
                .count() == allSubscriberToSave.size();
    }

    private boolean subscriberIsSavedWithValidValue(Subscriber subscriber) {
        return isEqual(subscriber, allSubscriber.get(subscriber.id()).block());
    }

    private long numberOfSubscriber() {
        return allSubscriber.getAll().count().block();
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

    private Subscriber createRandomSubscriber() {
        return new Subscriber(UUID.randomUUID(), "random" + random.nextInt() + "@gmail.com");
    }

}
