package com.avella.example.newslettersubscriptionexample.application.commands;

import com.avella.example.newslettersubscriptionexample.application.commands.shared.Command;

import java.util.UUID;

public record NotifySubscriberOfNewsletterCommand(UUID newsletterId, String title, String link) implements Command {
}
