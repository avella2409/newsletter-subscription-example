package com.avella.example.newslettersubscriptionexample.clients.message.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.ChangeSubscriberPlanCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.RabbitMQAbstractMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.SubscriberPlanChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

public class SubscriberPlanChangedMessageHandler extends RabbitMQAbstractMessageHandler<SubscriberPlanChangedEvent> {

    private final CommandHandler<ChangeSubscriberPlanCommand> handler;
    private final ObjectMapper objectMapper;

    public SubscriberPlanChangedMessageHandler(String queueName, Receiver receiver,
                                               CommandHandler<ChangeSubscriberPlanCommand> handler,
                                               ObjectMapper objectMapper) {
        super(queueName, receiver);
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getMessageIdentifier(SubscriberPlanChangedEvent message) {
        return message.eventId().toString();
    }

    @Override
    protected Mono<Void> handleMessage(SubscriberPlanChangedEvent message) {
        return handler.handle(new ChangeSubscriberPlanCommand(message.subscriberId(), message.planLevel()));
    }

    @Override
    protected SubscriberPlanChangedEvent parse(String json) {
        try {
            return objectMapper.readValue(json, SubscriberPlanChangedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
