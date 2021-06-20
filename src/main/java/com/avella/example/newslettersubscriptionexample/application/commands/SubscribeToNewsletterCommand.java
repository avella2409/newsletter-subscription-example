package com.avella.example.newslettersubscriptionexample.application.commands;

import com.avella.example.newslettersubscriptionexample.application.commands.shared.Command;

import java.util.UUID;

public record SubscribeToNewsletterCommand(UUID subscriberId, UUID newsletterId) implements Command {
}
