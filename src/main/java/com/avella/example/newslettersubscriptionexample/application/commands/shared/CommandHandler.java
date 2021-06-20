package com.avella.example.newslettersubscriptionexample.application.commands.shared;

import reactor.core.publisher.Mono;

public interface CommandHandler<C extends Command> {
    Mono<Void> handle(C command);
}
