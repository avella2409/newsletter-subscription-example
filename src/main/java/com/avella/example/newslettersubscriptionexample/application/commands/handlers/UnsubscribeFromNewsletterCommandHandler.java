package com.avella.example.newslettersubscriptionexample.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.UnsubscribeFromNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Mono;

public class UnsubscribeFromNewsletterCommandHandler implements CommandHandler<UnsubscribeFromNewsletterCommand> {

    private final SubscriberRepository allSubscriber;

    public UnsubscribeFromNewsletterCommandHandler(SubscriberRepository allSubscriber) {
        this.allSubscriber = allSubscriber;
    }

    @Override
    public Mono<Void> handle(UnsubscribeFromNewsletterCommand command) {
        return allSubscriber.get(command.subscriberId())
                .switchIfEmpty(Mono.error(new SubscriberDoesntExistException(command.subscriberId())))
                .doOnNext(subscriber -> subscriber.unsubscribeFromNewsletter(command.newsletterId()))
                .flatMap(allSubscriber::add);
    }
}
