package com.avella.example.newslettersubscriptionexample.infrastructure.notification;

import com.avella.example.newslettersubscriptionexample.domain.notification.Notification;
import com.avella.example.newslettersubscriptionexample.domain.notification.NotificationSender;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

public class SendGridEmailNotificationSender implements NotificationSender {

    public static final int EMAIL_SUCCESSFULLY_SEND_CODE = 202;
    private final String senderEmail;
    private final SendGrid sendGrid;

    public SendGridEmailNotificationSender(String apiKey, String senderEmail) {
        this.senderEmail = senderEmail;
        sendGrid = new SendGrid(apiKey);
    }

    @Override
    public Mono<Void> send(Notification notification) {
        return Mono.defer(() -> {
            try {
                Mail mail = buildMail(notification);
                Request request = buildRequest(mail);
                Response response = sendGrid.api(request);

                if (response.getStatusCode() != EMAIL_SUCCESSFULLY_SEND_CODE)
                    return Mono.error(new RuntimeException("Invalid status code " + response.getStatusCode()));
                return Mono.empty();
            } catch (IOException e) {
                return Mono.error(e);
            }
        })
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Request buildRequest(Mail email) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(email.build());
        return request;
    }

    private Mail buildMail(Notification notification) {
        return new Mail(new Email(senderEmail),
                notification.notificationContent().title(),
                new Email(notification.recipient()),
                new Content("text/plain", notification.notificationContent().body()));
    }
}
