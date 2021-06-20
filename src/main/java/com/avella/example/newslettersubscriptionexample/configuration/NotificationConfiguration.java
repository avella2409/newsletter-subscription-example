package com.avella.example.newslettersubscriptionexample.configuration;

import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationSender;
import com.avella.example.newslettersubscriptionexample.infrastructure.notification.SendGridEmailNotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfiguration {

    @Bean
    public NotificationSender notificationSender(@Value("${sendgrid.api.key}") String apiKey,
                                                 @Value("${sendgrid.sender.email}") String senderEmail) {
        return new SendGridEmailNotificationSender(apiKey, senderEmail);
    }
}
