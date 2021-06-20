package com.avella.example.newslettersubscriptionexample.configuration.postgre;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgreConfiguration {

    @Bean
    public PostgreInfo postgreInfo(@Value("${postgres.hostname}") String hostname,
                                   @Value("${postgres.port}") int port,
                                   @Value("${postgres.username}") String username,
                                   @Value("${postgres.password}") String password,
                                   @Value("${postgres.db.name}") String dbName) {
        return new PostgreInfo(hostname, port, username, password, dbName);
    }

    @Bean
    public PostgresqlConnectionFactory connectionFactory(PostgreInfo postgreInfo) {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(postgreInfo.host())
                        .port(postgreInfo.port())
                        .database(postgreInfo.db())
                        .username(postgreInfo.username())
                        .password(postgreInfo.password())
                        .build());
    }
}
