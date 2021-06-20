package com.avella.example.newslettersubscriptionexample.configuration.rabbitmq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Sender sender(RabbitMQConnectionInfo rabbitMQConnectionInfo) {
        return new RabbitMQSender(rabbitMQConnectionInfo).getSender();
    }

    @Bean
    public Receiver receiver(RabbitMQConnectionInfo rabbitMQConnectionInfo) {
        return new RabbitMQReceiver(rabbitMQConnectionInfo).getReceiver();
    }

    @Bean
    public RabbitMQConnectionInfo rabbitMQConnectionInfo(@Value("${message.hostname}") String hostname,
                                                         @Value("${message.port}") int port,
                                                         @Value("${message.username}") String username,
                                                         @Value("${message.password}") String password,
                                                         @Value("${message.usessl}") boolean useSSL) {
        return new RabbitMQConnectionInfo(hostname, port, username, password, useSSL);
    }
}
