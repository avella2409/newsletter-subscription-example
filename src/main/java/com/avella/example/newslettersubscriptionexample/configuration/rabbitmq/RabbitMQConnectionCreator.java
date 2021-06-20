package com.avella.example.newslettersubscriptionexample.configuration.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnectionCreator {

    private RabbitMQConnectionCreator() {
    }

    public static Connection create(RabbitMQConnectionInfo rabbitMQConnectionInfo)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQConnectionInfo.hostname());
        factory.setPort(rabbitMQConnectionInfo.port());
        factory.setUsername(rabbitMQConnectionInfo.username());
        factory.setPassword(rabbitMQConnectionInfo.password());
        if (rabbitMQConnectionInfo.useSSL())
            factory.useSslProtocol();
        return factory.newConnection();
    }
}
