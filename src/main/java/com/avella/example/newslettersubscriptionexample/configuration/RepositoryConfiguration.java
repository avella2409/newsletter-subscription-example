package com.avella.example.newslettersubscriptionexample.configuration;

import com.avella.example.newslettersubscriptionexample.domain.subscriber.SubscriberRepository;
import com.avella.example.newslettersubscriptionexample.infrastructure.persistence.postgre.PostgreSubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public SubscriberRepository subscriberRepository(PostgresqlConnectionFactory connectionFactory,
                                                     ObjectMapper objectMapper) {
        return new PostgreSubscriberRepository(connectionFactory, objectMapper);
    }
}
