package com.avella.example.newslettersubscriptionexample.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.SubscribeToNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Mono;

public class SubscribeToNewsletterCommandHandler implements CommandHandler<SubscribeToNewsletterCommand> {

    private final SubscriberRepository allSubscriber;

    public SubscribeToNewsletterCommandHandler(SubscriberRepository allSubscriber) {
        this.allSubscriber = allSubscriber;
    }

    @Override
    public Mono<Void> handle(SubscribeToNewsletterCommand command) {
        return allSubscriber.get(command.subscriberId())
                .switchIfEmpty(Mono.error(new SubscriberDoesntExistException(command.subscriberId())))
                .doOnNext(subscriber -> subscriber.subscribeToNewsletter(command.newsletterId()))
                .flatMap(allSubscriber::add);
    }
}
