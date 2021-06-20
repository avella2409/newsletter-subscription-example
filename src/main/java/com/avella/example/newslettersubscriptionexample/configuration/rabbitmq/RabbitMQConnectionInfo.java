package com.avella.example.newslettersubscriptionexample.configuration.rabbitmq;

public record RabbitMQConnectionInfo(String hostname, int port, String username, String password, boolean useSSL) {
}
