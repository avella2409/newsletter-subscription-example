package com.avella.example.newslettersubscriptionexample.domain.shared;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public abstract class Entity {

    private final long surrogateId;
    private final LocalDateTime lastUpdateTime;
    private final LocalDateTime creationTime;
    private final long version;
    private final UUID id;

    protected Entity(UUID id) {
        this.surrogateId = 0;
        this.lastUpdateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        this.creationTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        this.version = 0;
        this.id = id;
    }

    protected Entity(EntityMemento memento) {
        this.surrogateId = memento.surrogateId();
        this.lastUpdateTime = memento.lastUpdateTime();
        this.creationTime = memento.creationTime();
        this.version = memento.version();
        this.id = memento.id();
    }

    public UUID id() {
        return this.id;
    }

    protected EntityMemento createEntityMemento() {
        return new EntityMemento(surrogateId, lastUpdateTime, creationTime, version, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return surrogateId == entity.surrogateId && version == entity.version
                && Objects.equals(lastUpdateTime, entity.lastUpdateTime)
                && Objects.equals(creationTime, entity.creationTime)
                && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surrogateId, lastUpdateTime, creationTime, version, id);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "surrogateId=" + surrogateId +
                ", lastUpdateTime=" + lastUpdateTime +
                ", creationTime=" + creationTime +
                ", version=" + version +
                ", id=" + id +
                '}';
    }
}
