package com.avella.example.newslettersubscriptionexample.integration.clients.message;

import com.avella.example.newslettersubscriptionexample.application.commands.ChangeSubscriberPlanCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.InstantiateSubscriberCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.NotifySubscriberOfNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.ChangeSubscriberPlanCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.InstantiateSubscriberCommandHandler;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.NotifySubscriberOfNewsletterCommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewSubscriberRegisteredMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewsletterPublishedMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.SubscriberPlanChangedMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.NewSubscriberRegisteredEvent;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.NewsletterPublishedEvent;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.SubscriberPlanChangedEvent;
import com.avella.example.newslettersubscriptionexample.configuration.rabbitmq.RabbitMQConnectionInfo;
import com.avella.example.newslettersubscriptionexample.configuration.rabbitmq.RabbitMQReceiver;
import com.avella.example.newslettersubscriptionexample.configuration.rabbitmq.RabbitMQSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@Tag("integration")
class RabbitMQHandlersTest {

    @Container
    public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3"));

    private Receiver receiver;
    private Sender sender;
    private ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        var connectionInfo = new RabbitMQConnectionInfo(rabbitMQContainer.getHost(),
                rabbitMQContainer.getAmqpPort(),
                rabbitMQContainer.getAdminUsername(),
                rabbitMQContainer.getAdminPassword(),
                false);
        sender = new RabbitMQSender(connectionInfo).getSender();
        receiver = new RabbitMQReceiver(connectionInfo).getReceiver();
        objectMapper = new ObjectMapper();
    }

    @Test
    void newSubscriberRegisteredHandler() {
        String queueName = "new-subscriber-registered";
        var event = new NewSubscriberRegisteredEvent(UUID.randomUUID(), UUID.randomUUID(),
                "random@gmail.com");

        declareQueue(queueName);
        sendMessageToBroker(queueName, eventToJson(event));

        var handlerResponse = TestPublisher.<Void>createCold().complete();
        var commandHandler = mock(InstantiateSubscriberCommandHandler.class);
        var messageHandler = new NewSubscriberRegisteredMessageHandler(queueName, receiver, commandHandler, objectMapper);

        when(commandHandler.handle(new InstantiateSubscriberCommand(event.subscriberId(), event.subscriberEmail())))
                .thenReturn(handlerResponse.mono());

        StepVerifier.create(messageHandler.startProcessingMessage().take(Duration.ofSeconds(1)))
                .expectNextMatches(identifier -> identifier.equals(event.eventId().toString()))
                .verifyComplete();

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void subscriberPlanChangedHandler() {
        String queueName = "subscriber-plan-changed";
        var event = new SubscriberPlanChangedEvent(UUID.randomUUID(), UUID.randomUUID(), 1);

        declareQueue(queueName);
        sendMessageToBroker(queueName, eventToJson(event));

        var handlerResponse = TestPublisher.<Void>createCold().complete();
        var commandHandler = mock(ChangeSubscriberPlanCommandHandler.class);
        var messageHandler = new SubscriberPlanChangedMessageHandler(queueName, receiver, commandHandler, objectMapper);

        when(commandHandler.handle(new ChangeSubscriberPlanCommand(event.subscriberId(), event.planLevel())))
                .thenReturn(handlerResponse.mono());

        StepVerifier.create(messageHandler.startProcessingMessage().take(Duration.ofSeconds(1)))
                .expectNextMatches(identifier -> identifier.equals(event.eventId().toString()))
                .verifyComplete();

        handlerResponse.assertWasSubscribed();
    }

    @Test
    void newsletterPublishedHandler() {
        String queueName = "newsletter-published";
        var event = new NewsletterPublishedEvent(UUID.randomUUID(), UUID.randomUUID(),
                "Random Title", "https://www.random.com");

        declareQueue(queueName);
        sendMessageToBroker(queueName, eventToJson(event));

        var handlerResponse = TestPublisher.<Void>createCold().complete();
        var commandHandler = mock(NotifySubscriberOfNewsletterCommandHandler.class);
        var messageHandler = new NewsletterPublishedMessageHandler(queueName, receiver, commandHandler, objectMapper);

        when(commandHandler.handle(new NotifySubscriberOfNewsletterCommand(event.newsletterId(),
                event.title(), event.link())))
                .thenReturn(handlerResponse.mono());

        StepVerifier.create(messageHandler.startProcessingMessage().take(Duration.ofSeconds(1)))
                .expectNextMatches(identifier -> identifier.equals(event.eventId().toString()))
                .verifyComplete();

        handlerResponse.assertWasSubscribed();
    }

    private void declareQueue(String queueName) {
        sender.declareQueue(QueueSpecification.queue(queueName)).block();
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
}
