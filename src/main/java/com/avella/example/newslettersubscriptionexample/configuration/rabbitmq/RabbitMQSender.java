package com.avella.example.newslettersubscriptionexample.configuration.rabbitmq;

import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

public class RabbitMQSender {

    private volatile Sender sender;

    private final RabbitMQConnectionInfo connectionInfo;

    public RabbitMQSender(RabbitMQConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    private void initSender() {
        try {
            sender = RabbitFlux.createSender(new SenderOptions()
                    .connectionMono(Mono.just(RabbitMQConnectionCreator.create(connectionInfo))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Sender getSender() {
        if (sender == null)
            initSender();
        return sender;
    }
}
