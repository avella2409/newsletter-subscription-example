package com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre;

import com.avella.example.newslettersubscriptionexample.domain.shared.ConcurrentEntityModificationException;
import com.avella.example.newslettersubscriptionexample.domain.shared.EntityIdAlreadyExistException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.Subscriber;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.postgresql.api.PostgresqlStatement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PostgreSubscriberRepository implements SubscriberRepository {

    private final Mono<PostgresqlConnection> connection;
    private final ObjectMapper objectMapper;
    private final PostgreSubscriberMapper mapper;

    public PostgreSubscriberRepository(PostgresqlConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        connection = connectionFactory.create();
        mapper = new PostgreSubscriberMapper(objectMapper);
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> add(Subscriber subscriber) {
        return connection.flatMap(co -> {
            PostgreSubscriber postgreSubscriber = PostgreSubscriber.fromSubscriber(subscriber, objectMapper);
            Mono<Void> interaction = postgreSubscriber.entityMemento().version() == 0 ?
                    save(co, postgreSubscriber)
                    : update(co, postgreSubscriber);
            return interaction
                    .doFinally(signalType -> co.close().subscribe());
        });
    }

    @Override
    public Mono<Subscriber> get(UUID subscriberId) {
        return connection.flatMap(co -> co.createStatement("SELECT * FROM subscriber WHERE id=$1")
                .bind("$1", subscriberId)
                .execute()
                .flatMap(result -> result.map(mapper))
                .next()
                .doFinally(signalType -> co.close().subscribe())
        );
    }

    @Override
    public Flux<Subscriber> getAllWithASubscriptionToTheNewsletter(UUID newsletterId) {
        return connection.flatMapMany(co -> co.createStatement("""
                SELECT s.*  FROM subscriber s
                CROSS JOIN LATERAL json_array_elements(s.allSubscription) as j
                WHERE j->>'newsletterId'=$1
                """)
                .bind("$1", newsletterId.toString())
                .execute()
                .flatMap(result -> result.map(mapper))
                .doFinally(signalType -> co.close().subscribe())
        );
    }

    private Mono<Void> update(PostgresqlConnection co, PostgreSubscriber postgreSubscriber) {
        return updateStatement(co, postgreSubscriber)
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .flatMap(nbRowUpdated -> nbRowUpdated == 0 ?
                        Mono.error(new ConcurrentEntityModificationException(postgreSubscriber.entityMemento().id()))
                        : Mono.empty())
                .then();
    }

    private Mono<Void> save(PostgresqlConnection co, PostgreSubscriber postgreSubscriber) {
        return saveStatement(co, postgreSubscriber)
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .flatMap(nbRowUpdated -> nbRowUpdated == 0 ?
                        Mono.error(new EntityIdAlreadyExistException(postgreSubscriber.entityMemento().id()))
                        : Mono.empty())
                .then();
    }

    private PostgresqlStatement updateStatement(PostgresqlConnection co, PostgreSubscriber postgreSubscriber) {
        return co.createStatement("""
                UPDATE subscriber 
                SET lastUpdateTime=current_timestamp, 
                version=$1,
                email=$2,
                plan=$3,
                allSubscription=$4::json,
                lastSubscriptionNumber=$5
                where surrogateId=$6 and version=$7
                """)
                .bind("$1", postgreSubscriber.entityMemento().version() + 1)
                .bind("$2", postgreSubscriber.email())
                .bind("$3", postgreSubscriber.planLevel())
                .bind("$4", postgreSubscriber.allSubscriptionJson())
                .bind("$5", postgreSubscriber.lastSubscriptionNumber())
                .bind("$6", postgreSubscriber.entityMemento().surrogateId())
                .bind("$7", postgreSubscriber.entityMemento().version());
    }

    private PostgresqlStatement saveStatement(PostgresqlConnection co, PostgreSubscriber postgreSubscriber) {
        return co.createStatement("""
                INSERT INTO subscriber VALUES(
                DEFAULT, 
                current_timestamp, 
                current_timestamp, 
                $1, 
                $2, 
                $3,
                $4,
                $5::json,
                $6
                ) 
                ON CONFLICT DO NOTHING            
                """)
                .bind("$1", postgreSubscriber.entityMemento().version() + 1)
                .bind("$2", postgreSubscriber.entityMemento().id())
                .bind("$3", postgreSubscriber.email())
                .bind("$4", postgreSubscriber.planLevel())
                .bind("$5", postgreSubscriber.allSubscriptionJson())
                .bind("$6", postgreSubscriber.lastSubscriptionNumber());
    }

    public Flux<Subscriber> getAll() {
        return connection.flatMapMany(co -> co.createStatement("SELECT * FROM subscriber")
                .execute()
                .flatMap(result -> result.map(mapper))
                .doFinally(signalType -> co.close().subscribe())
        );
    }

    public Mono<Void> deleteTable() {
        return connection.flatMap(co -> co.createStatement("DROP TABLE subscriber")
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .then()
                .doFinally(signalType -> co.close().subscribe())
        );
    }

    public Mono<Void> createTable() {
        return connection.flatMap(co -> co.createStatement("""
                CREATE TABLE subscriber
                                        (
                                        surrogateId serial primary key,
                                		lastUpdateTime timestamp not null,
                                		creationTime timestamp not null,
                                		version bigint not null,
                                		id UUID not null,
                                        email varchar(255) not null,
                                        plan int not null,
                                        allSubscription json not null,
                                        lastSubscriptionNumber bigint not null,
                                        unique(id)
                                        )
                """)
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .then()
                .doFinally(signalType -> co.close().subscribe())
        );
    }
}
