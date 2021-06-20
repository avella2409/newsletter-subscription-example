package com.avella.example.newslettersubscriptionexample.clients.message.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.NotifySubscriberOfNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.RabbitMQAbstractMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.events.NewsletterPublishedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

public class NewsletterPublishedMessageHandler extends RabbitMQAbstractMessageHandler<NewsletterPublishedEvent> {

    private final CommandHandler<NotifySubscriberOfNewsletterCommand> handler;
    private final ObjectMapper objectMapper;

    public NewsletterPublishedMessageHandler(String queueName, Receiver receiver,
                                             CommandHandler<NotifySubscriberOfNewsletterCommand> handler,
                                             ObjectMapper objectMapper) {
        super(queueName, receiver);
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getMessageIdentifier(NewsletterPublishedEvent message) {
        return message.eventId().toString();
    }

    @Override
    protected Mono<Void> handleMessage(NewsletterPublishedEvent message) {
        return handler.handle(new NotifySubscriberOfNewsletterCommand(message.newsletterId(), message.title(),
                message.link()));
    }

    @Override
    protected NewsletterPublishedEvent parse(String json) {
        try {
            return objectMapper.readValue(json, NewsletterPublishedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
