package com.avella.example.newslettersubscriptionexample.infrastructure.persistence.inmemory;

import com.avella.example.newslettersubscriptionexample.domain.shared.ConcurrentEntityModificationException;
import com.avella.example.newslettersubscriptionexample.domain.shared.EntityIdAlreadyExistException;
import com.avella.example.newslettersubscriptionexample.domain.shared.EntityMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberMemento;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySubscriberRepository implements SubscriberRepository {

    private final Map<UUID, SubscriberMemento> allSubscriber = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> add(Subscriber subscriber) {
        return Mono.defer(() -> {
            SubscriberMemento memento = subscriber.createMemento();
            return memento.entityMemento().version() == 0 ? save(memento) : update(memento);
        });
    }

    @Override
    public Mono<Subscriber> get(UUID subscriberId) {
        return Mono.defer(() -> allSubscriber.values().stream()
                .filter(memento -> memento.entityMemento().id().equals(subscriberId))
                .findFirst()
                .map(memento -> Mono.just(Subscriber.fromMemento(memento)))
                .orElse(Mono.empty()));
    }

    @Override
    public Flux<Subscriber> getAllWithASubscriptionToTheNewsletter(UUID newsletterId) {
        return Flux.defer(() -> Flux.fromStream(allSubscriber.values().stream()
                .filter(memento -> memento.allSubscription().stream()
                        .anyMatch(subscription -> subscription.newsletterId().equals(newsletterId))))
                .map(Subscriber::fromMemento));
    }

    public Flux<Subscriber> getAll() {
        return Flux.defer(() -> Flux.fromIterable(allSubscriber.values()).map(Subscriber::fromMemento));
    }

    private SubscriberMemento createIncreasedVersion(SubscriberMemento memento) {
        return new SubscriberMemento(createEntityMemento(memento), memento.email(), memento.plan(),
                memento.allSubscription(), memento.lastSubscriptionNumber());
    }

    private EntityMemento createEntityMemento(SubscriberMemento memento) {
        boolean isFirst = memento.entityMemento().version() == 0;
        return new EntityMemento(memento.entityMemento().surrogateId(), LocalDateTime.now(ZoneOffset.UTC),
                isFirst ? LocalDateTime.now(ZoneOffset.UTC) : memento.entityMemento().creationTime(),
                memento.entityMemento().version() + 1, memento.entityMemento().id());
    }

    private Mono<Void> save(SubscriberMemento memento) {
        return allSubscriber.putIfAbsent(memento.entityMemento().id(), createIncreasedVersion(memento)) == null ?
                Mono.empty()
                : Mono.error(new EntityIdAlreadyExistException(memento.entityMemento().id()));
    }

    private Mono<Void> update(SubscriberMemento memento) {
        SubscriberMemento current = allSubscriber.get(memento.entityMemento().id());
        if (current.entityMemento().version() != memento.entityMemento().version())
            return Mono.error(new ConcurrentEntityModificationException(memento.entityMemento().id()));

        allSubscriber.put(memento.entityMemento().id(), createIncreasedVersion(memento));
        return Mono.empty();
    }
}
