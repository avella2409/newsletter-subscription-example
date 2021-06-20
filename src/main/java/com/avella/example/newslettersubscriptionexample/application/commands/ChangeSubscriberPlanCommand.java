package com.avella.example.newslettersubscriptionexample.application.commands;

import com.avella.example.newslettersubscriptionexample.application.commands.shared.Command;

import java.util.UUID;

public record ChangeSubscriberPlanCommand(UUID subscriberId, int planLevel) implements Command {
}
