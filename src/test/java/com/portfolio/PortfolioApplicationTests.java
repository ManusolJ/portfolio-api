package com.portfolio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Boots the full context against a throwaway pgvector Postgres; SMTP is configured but never contacted. */
@SpringBootTest(properties = {"MAIL_HOST=localhost", "MAIL_USER=test", "MAIL_PASSWORD=test",
        "MAIL_FROM=portfolio@example.com", "CONTACT_TO=inbox@example.com",})
@Testcontainers
class PortfolioApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("pgvector/pgvector:pg17");

    @Test
    void contextLoads() {
    }
}
