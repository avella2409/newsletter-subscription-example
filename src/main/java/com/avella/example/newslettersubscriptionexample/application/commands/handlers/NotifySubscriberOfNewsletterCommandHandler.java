package com.avella.example.newslettersubscriptionexample.application.commands.handlers;

import com.avella.example.newslettersubscriptionexample.application.commands.NotifySubscriberOfNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.domain.notification.Notification;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationContent;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationSender;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Mono;

public class NotifySubscriberOfNewsletterCommandHandler implements CommandHandler<NotifySubscriberOfNewsletterCommand> {

    private final SubscriberRepository allSubscriber;
    private final NotificationSender notificationSender;
    private final TimeService timeService;

    public NotifySubscriberOfNewsletterCommandHandler(SubscriberRepository allSubscriber,
                                                      NotificationSender notificationSender,
                                                      TimeService timeService) {
        this.allSubscriber = allSubscriber;
        this.notificationSender = notificationSender;
        this.timeService = timeService;
    }

    @Override
    public Mono<Void> handle(NotifySubscriberOfNewsletterCommand command) {
        return Mono.defer(() -> {
            NotificationContent notificationContent = buildNotificationContent(command);
            return allSubscriber.getAllWithASubscriptionToTheNewsletter(command.newsletterId())
                    .flatMap(subscriber -> notificationSender.send(new Notification(subscriber.email(), notificationContent))
                            .onErrorResume(err -> Mono.empty()))
                    .then();
        });
    }

    private NotificationContent buildNotificationContent(NotifySubscriberOfNewsletterCommand command) {
        return NotificationContent.create(timeService.getCurrentTime(), command.title(), command.link());
    }
}
