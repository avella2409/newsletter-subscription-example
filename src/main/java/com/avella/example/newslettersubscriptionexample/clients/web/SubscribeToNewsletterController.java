package com.avella.example.newslettersubscriptionexample.clients.web;

import com.avella.example.newslettersubscriptionexample.application.commands.SubscribeToNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.AlreadyHasASubscriptionToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.PlanLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class SubscribeToNewsletterController {

    private final CommandHandler<SubscribeToNewsletterCommand> handler;

    public SubscribeToNewsletterController(CommandHandler<SubscribeToNewsletterCommand> handler) {
        this.handler = handler;
    }

    @PostMapping("/subscribe/{subscriberId}/{newsletterId}")
    public Mono<ResponseEntity<Void>> subscribeToNewsletter(@PathVariable UUID subscriberId,
                                                            @PathVariable UUID newsletterId) {
        return handler.handle(new SubscribeToNewsletterCommand(subscriberId, newsletterId))
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorReturn(SubscriberDoesntExistException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(AlreadyHasASubscriptionToTheNewsletterException.class,
                        ResponseEntity.status(HttpStatus.CONFLICT).build())
                .onErrorReturn(PlanLimitExceededException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}
