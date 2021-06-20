package com.avella.example.newslettersubscriptionexample.clients.message.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.InstantiateSubscriberCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.RabbitMQAbstractMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.NewSubscriberRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

public class NewSubscriberRegisteredMessageHandler extends RabbitMQAbstractMessageHandler<NewSubscriberRegisteredEvent> {

    private final CommandHandler<InstantiateSubscriberCommand> handler;
    private final ObjectMapper objectMapper;

    public NewSubscriberRegisteredMessageHandler(String queueName, Receiver receiver,
                                                 CommandHandler<InstantiateSubscriberCommand> handler,
                                                 ObjectMapper objectMapper) {
        super(queueName, receiver);
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getMessageIdentifier(NewSubscriberRegisteredEvent message) {
        return message.eventId().toString();
    }

    @Override
    protected Mono<Void> handleMessage(NewSubscriberRegisteredEvent message) {
        return handler.handle(new InstantiateSubscriberCommand(message.subscriberId(), message.subscriberEmail()));
    }

    @Override
    protected NewSubscriberRegisteredEvent parse(String json) {
        try {
            return objectMapper.readValue(json, NewSubscriberRegisteredEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
