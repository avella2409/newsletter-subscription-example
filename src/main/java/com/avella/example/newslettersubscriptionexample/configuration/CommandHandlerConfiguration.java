package com.avella.example.newslettersubscriptionexample.configuration;

import com.avella.example.newslettersubscriptionexample.application.commands.*;
import com.avella.example.newslettersubscriptionexample.application.commands.handlers.*;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationSender;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandHandlerConfiguration {

    @Bean
    public CommandHandler<ChangeSubscriberPlanCommand>
    changeSubscriberPlanCommandHandler(SubscriberRepository allSubscriber) {
        return new ChangeSubscriberPlanCommandHandler(allSubscriber);
    }

    @Bean
    public CommandHandler<InstantiateSubscriberCommand>
    instantiateSubscriberCommandHandler(SubscriberRepository allSubscriber) {
        return new InstantiateSubscriberCommandHandler(allSubscriber);
    }

    @Bean
    public CommandHandler<NotifySubscriberOfNewsletterCommand>
    notifySubscriberOfNewsletterCommandHandler(SubscriberRepository allSubscriber,
                                               NotificationSender notificationSender,
                                               TimeService timeService) {
        return new NotifySubscriberOfNewsletterCommandHandler(allSubscriber, notificationSender, timeService);
    }

    @Bean
    public CommandHandler<SubscribeToNewsletterCommand>
    subscribeToNewsletterCommandHandler(SubscriberRepository allSubscriber) {
        return new SubscribeToNewsletterCommandHandler(allSubscriber);
    }

    @Bean
    public CommandHandler<UnsubscribeFromNewsletterCommand>
    unsubscribeFromNewsletterCommandHandler(SubscriberRepository allSubscriber) {
        return new UnsubscribeFromNewsletterCommandHandler(allSubscriber);
    }

}
