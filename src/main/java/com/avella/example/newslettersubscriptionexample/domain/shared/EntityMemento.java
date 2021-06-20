package com.avella.example.newslettersubscriptionexample.domain.shared;

import java.time.LocalDateTime;
import java.util.UUID;

public record EntityMemento(long surrogateId, LocalDateTime lastUpdateTime, LocalDateTime creationTime, long version,
                            UUID id) {
}
