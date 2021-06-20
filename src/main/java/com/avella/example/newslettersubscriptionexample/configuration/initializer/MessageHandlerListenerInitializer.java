package com.avella.example.newslettersubscriptionexample.configuration.initializer;

import com.avella.example.newslettersubscriptionexample.NewsletterSubscriptionExampleApplication;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewSubscriberRegisteredMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewsletterPublishedMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.SubscriberPlanChangedMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class MessageHandlerListenerInitializer {
    private final Logger log = LoggerFactory.getLogger(NewsletterSubscriptionExampleApplication.class);

    private final NewsletterPublishedMessageHandler newsletterPublishedMessageHandler;
    private final NewSubscriberRegisteredMessageHandler newSubscriberRegisteredMessageHandler;
    private final SubscriberPlanChangedMessageHandler subscriberPlanChangedMessageHandler;

    public MessageHandlerListenerInitializer(NewsletterPublishedMessageHandler newsletterPublishedMessageHandler,
                                             NewSubscriberRegisteredMessageHandler newSubscriberRegisteredMessageHandler,
                                             SubscriberPlanChangedMessageHandler subscriberPlanChangedMessageHandler) {
        this.newsletterPublishedMessageHandler = newsletterPublishedMessageHandler;
        this.newSubscriberRegisteredMessageHandler = newSubscriberRegisteredMessageHandler;
        this.subscriberPlanChangedMessageHandler = subscriberPlanChangedMessageHandler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Start listening to message...");

        newsletterPublishedMessageHandler.startProcessingMessage().subscribe();
        newSubscriberRegisteredMessageHandler.startProcessingMessage().subscribe();
        subscriberPlanChangedMessageHandler.startProcessingMessage().subscribe();
    }
}
