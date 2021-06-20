package com.avella.example.newslettersubscriptionexample.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.ChangeSubscriberPlanCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.InvalidPlanLevelException;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.exceptions.SubscriberDoesntExistException;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberPlan;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ChangeSubscriberPlanCommandHandler implements CommandHandler<ChangeSubscriberPlanCommand> {

    private final SubscriberRepository allSubscriber;

    public ChangeSubscriberPlanCommandHandler(SubscriberRepository allSubscriber) {
        this.allSubscriber = allSubscriber;
    }

    @Override
    public Mono<Void> handle(ChangeSubscriberPlanCommand command) {
        Optional<SubscriberPlan> optionalSubscriberPlan = planLevelToSubscriberPlan(command.planLevel());
        if (optionalSubscriberPlan.isEmpty())
            return Mono.error(new InvalidPlanLevelException(command.planLevel()));

        return allSubscriber.get(command.subscriberId())
                .switchIfEmpty(Mono.error(new SubscriberDoesntExistException(command.subscriberId())))
                .doOnNext(subscriber -> subscriber.changePlan(optionalSubscriberPlan.get()))
                .flatMap(allSubscriber::add);
    }

    public Optional<SubscriberPlan> planLevelToSubscriberPlan(int planLevel) {
        if (planLevel == 0)
            return Optional.of(SubscriberPlan.FREE);
        else if (planLevel == 1)
            return Optional.of(SubscriberPlan.PREMIUM);
        return Optional.empty();
    }
}
