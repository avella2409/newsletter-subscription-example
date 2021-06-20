package com.avella.example.newslettersubscriptionexample.configuration;

import com.avella.example.newslettersubscriptionexample.application.commands.ChangeSubscriberPlanCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.InstantiateSubscriberCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.NotifySubscriberOfNewsletterCommand;
import com.avella.example.newslettersubscriptionexample.application.commands.shared.CommandHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewSubscriberRegisteredMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.NewsletterPublishedMessageHandler;
import com.avella.example.newslettersubscriptionexample.clients.message.handlers.SubscriberPlanChangedMessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.Receiver;

@Configuration
public class MessageHandlerConfiguration {

    @Bean
    public NewsletterPublishedMessageHandler
    newsletterPublishedMessageHandler(@Value("${newsletter.published.queue}") String queueName,
                                      Receiver receiver,
                                      CommandHandler<NotifySubscriberOfNewsletterCommand> handler,
                                      ObjectMapper objectMapper) {
        return new NewsletterPublishedMessageHandler(queueName, receiver, handler, objectMapper);
    }

    @Bean
    public NewSubscriberRegisteredMessageHandler
    newSubscriberRegisteredMessageHandler(@Value("${new.subscriber.queue}") String queueName,
                                          Receiver receiver,
                                          CommandHandler<InstantiateSubscriberCommand> handler,
                                          ObjectMapper objectMapper) {
        return new NewSubscriberRegisteredMessageHandler(queueName, receiver, handler, objectMapper);
    }

    @Bean
    public SubscriberPlanChangedMessageHandler
    subscriberPlanChangedMessageHandler(@Value("${subscriber.plan.changed.queue}") String queueName,
                                        Receiver receiver,
                                        CommandHandler<ChangeSubscriberPlanCommand> handler,
                                        ObjectMapper objectMapper) {
        return new SubscriberPlanChangedMessageHandler(queueName, receiver, handler, objectMapper);
    }
}
