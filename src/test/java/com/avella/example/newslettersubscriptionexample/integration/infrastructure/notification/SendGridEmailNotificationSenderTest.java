package com.avella.example.newslettersubscriptionexample.integration.infrastructure.notification;

import com.avella.example.newslettersubscriptionexample.domain.notification.Notification;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationContent;
import com.avella.example.newslettersubscriptionexample.infrastructure.notification.SendGridEmailNotificationSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@Tag("integration")
class SendGridEmailNotificationSenderTest {

    @Test
    void sendEmail() {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        String senderEmail = System.getenv("SENDGRID_SENDER_EMAIL");
        String recipientEmail = System.getenv("RECIPIENT_EMAIL");

        Notification notification = new Notification(recipientEmail,
                new NotificationContent("Random title", "Random body"));

        var notificationSender = new SendGridEmailNotificationSender(apiKey, senderEmail);

        StepVerifier.create(notificationSender.send(notification))
                .verifyComplete();
    }
}
