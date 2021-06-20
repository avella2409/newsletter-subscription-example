package com.avella.example.newslettersubscriptionexample.e2e;

import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.NewSubscriberRegisteredEvent;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.SubscriberPlanChangedEvent;
import com.avella.example.newslettersubscriptionexample.clients.web.models.SubscriptionJSON;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre.PostgreSubscriberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Testcontainers
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"production", "test"})
@ContextConfiguration(initializers = E2ETest.PropertyOverrideContextInitializer.class)
class E2ETest {

    @Container
    private static PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres"));

    @Container
    public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3"));

    private WebClient webClient;

    @Autowired
    private Sender sender;

    @Autowired
    private SubscriberRepository allSubscriber;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${newsletter.published.queue}")
    private String newsletterPublishedQueueName;

    @Value("${new.subscriber.queue}")
    private String newSubscriberQueueName;

    @Value("${subscriber.plan.changed.queue}")
    private String subscriberPlanChangedQueueName;

    @LocalServerPort
    private int serverPort;

    private PostgreSubscriberRepository postgreSubscriberRepository;

    @BeforeEach
    void init() {
        webClient = WebClient.builder().build();
        postgreSubscriberRepository = (PostgreSubscriberRepository) allSubscriber;
        deleteTable();
        createTable();
        declareAllQueue();
    }

    @Test
    void getAllSubscriberSubscription() throws InterruptedException {
        waitForAppToStartListening();

        UUID subscriberID = UUID.randomUUID();
        List<UUID> randomListOfNewsletterId = createRandomListOfNewsletterId(15);

        registerSubscriber(subscriberID);
        upgradeSubscriberPlan(subscriberID);
        subscribeToAllNewsletter(subscriberID, randomListOfNewsletterId);

        StepVerifier.create(webClient.get().uri("http://localhost:" + serverPort + "/subscriptions/" + subscriberID)
                .retrieve()
                .bodyToFlux(SubscriptionJSON.class))
                .recordWith(ArrayDeque::new)
                .thenConsumeWhile(t -> true)
                .expectRecordedMatches(recorded -> recorded.size() == randomListOfNewsletterId.size()
                        && recorded.stream()
                        .map(SubscriptionJSON::newsletterId)
                        .toList()
                        .containsAll(randomListOfNewsletterId))
                .verifyComplete();
    }

    private void waitForAppToStartListening() throws InterruptedException {
        System.out.println("Wait for app to start listening to MQ message...");
        Thread.sleep(15000);
    }

    private void upgradeSubscriberPlan(UUID subscriberID) throws InterruptedException {
        System.out.println("Change Subscriber Plan");
        sendMessageToBroker(subscriberPlanChangedQueueName,
                eventToJson(new SubscriberPlanChangedEvent(UUID.randomUUID(), subscriberID, 1)));
        Thread.sleep(1000);
    }

    private void registerSubscriber(UUID subscriberID) throws InterruptedException {
        System.out.println("Register Subscriber");
        sendMessageToBroker(newSubscriberQueueName,
                eventToJson(new NewSubscriberRegisteredEvent(UUID.randomUUID(), subscriberID,
                        "random@gmail.com")));
        Thread.sleep(1000);
    }

    private void subscribeToAllNewsletter(UUID subscriberID, List<UUID> randomListOfNewsletterId) {
        StepVerifier.create(Flux.fromIterable(randomListOfNewsletterId)
                .concatMap(newsletterId -> subscribeToNewsletter(subscriberID, newsletterId)))
                .verifyComplete();
    }

    private Mono<Void> subscribeToNewsletter(UUID subscriberId, UUID newsletterId) {
        return webClient.post().uri("http://localhost:" + serverPort + "/subscribe/" + subscriberId + "/" + newsletterId)
                .exchangeToMono(clientResponse -> clientResponse.statusCode().is2xxSuccessful() ?
                        Mono.empty() : Mono.error(new RuntimeException("Invalid Code")));
    }

    private List<UUID> createRandomListOfNewsletterId(int number) {
        return IntStream.range(0, number)
                .mapToObj(i -> UUID.randomUUID())
                .toList();
    }

    private void sendMessageToBroker(String queueName, String json) {
        sender.send(Mono.just(new OutboundMessage("", queueName, json.getBytes(StandardCharsets.UTF_8))))
                .block();
    }

    private String eventToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void declareAllQueue() {
        sender.declareQueue(QueueSpecification.queue(newsletterPublishedQueueName)).block();
        sender.declareQueue(QueueSpecification.queue(newSubscriberQueueName)).block();
        sender.declareQueue(QueueSpecification.queue(subscriberPlanChangedQueueName)).block();
    }

    public static class PropertyOverrideContextInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    "postgres.hostname=" + container.getHost(),
                    "postgres.port=" + Integer.parseInt(container.getJdbcUrl().split("localhost:")[1].split("/")[0]),
                    "postgres.username=" + container.getUsername(),
                    "postgres.password=" + container.getPassword(),
                    "postgres.db.name=" + container.getDatabaseName(),
                    "message.hostname=" + rabbitMQContainer.getHost(),
                    "message.port=" + rabbitMQContainer.getAmqpPort(),
                    "message.username=" + rabbitMQContainer.getAdminUsername(),
                    "message.password=" + rabbitMQContainer.getAdminPassword(),
                    "message.usessl=false");
        }
    }

    private void deleteTable() {
        try {
            postgreSubscriberRepository.deleteTable().block();
        } catch (Exception e) {
            System.out.println("Table already deleted");
        }
    }

    private void createTable() {
        try {
            postgreSubscriberRepository.createTable().block();
        } catch (Exception e) {
            System.out.println("Table already created");
        }
    }
}
