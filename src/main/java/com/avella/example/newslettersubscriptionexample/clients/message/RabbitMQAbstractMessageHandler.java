package com.avella.example.newslettersubscriptionexample.clients.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.ConsumeOptions;
import reactor.rabbitmq.Receiver;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public abstract class RabbitMQAbstractMessageHandler<MESSAGE_TYPE> {

    private final Logger log = LoggerFactory.getLogger(RabbitMQAbstractMessageHandler.class);

    private final String queueName;
    private final Receiver receiver;

    protected RabbitMQAbstractMessageHandler(String queueName, Receiver receiver) {
        this.queueName = queueName;
        this.receiver = receiver;
    }

    public Flux<String> startProcessingMessage() {
        return receiver.consumeManualAck(queueName, new ConsumeOptions().qos(1))
                .retryWhen(Retry.fixedDelay(60, Duration.ofSeconds(10)))
                .concatMap(delivery -> {
                    var message = parse(new String(delivery.getBody(), StandardCharsets.UTF_8));
                    String identifier = getMessageIdentifier(message);
                    return handleMessage(message)
                            .thenReturn(identifier)
                            .doOnError(err -> log.error("Error processing message {} {}", identifier, err.getMessage()))
                            .doOnSuccess(s -> log.info("Success processing message {}", identifier))
                            .doOnSubscribe(subscription -> log.info("Start processing message {}", identifier))
                            .onErrorResume(err -> Mono.empty())
                            .doFinally(signalType -> delivery.ack());
                });
    }

    protected abstract String getMessageIdentifier(MESSAGE_TYPE message);

    protected abstract Mono<Void> handleMessage(MESSAGE_TYPE message);

    protected abstract MESSAGE_TYPE parse(String json);

}
