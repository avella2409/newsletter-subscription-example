package com.avella.example.newslettersubscriptionexample.configuration.rabbitmq;

import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

public class RabbitMQReceiver {

    private volatile Receiver receiver;
    private final RabbitMQConnectionInfo connectionInfo;

    public RabbitMQReceiver(RabbitMQConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    private void initReceiver() {
        try {
            receiver = RabbitFlux.createReceiver(new ReceiverOptions()
                    .connectionMono(Mono.just(RabbitMQConnectionCreator.create(connectionInfo))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Receiver getReceiver() {
        if (receiver == null)
            initReceiver();
        return receiver;
    }
}
