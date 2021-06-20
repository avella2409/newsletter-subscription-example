package com.avella.example.newslettersubscriptionexample.clients.web;

import com.avella.example.newslettersubscriptionexample.application.commands.UnsubscribeFromNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.NotSubscribedToTheNewsletterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class UnsubscribeFromNewsletterController {

    private final CommandHandler<UnsubscribeFromNewsletterCommand> handler;

    public UnsubscribeFromNewsletterController(CommandHandler<UnsubscribeFromNewsletterCommand> handler) {
        this.handler = handler;
    }

    @DeleteMapping("/subscribe/{subscriberId}/{newsletterId}")
    public Mono<ResponseEntity<Void>> unsubscribeFromNewsletter(@PathVariable UUID subscriberId,
                                                            @PathVariable UUID newsletterId) {
        return handler.handle(new UnsubscribeFromNewsletterCommand(subscriberId, newsletterId))
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorReturn(SubscriberDoesntExistException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(NotSubscribedToTheNewsletterException.class,
                        ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
