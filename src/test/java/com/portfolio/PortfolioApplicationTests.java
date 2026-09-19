package com.portfolio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.junit.jupiter.api.Test;

@SpringBootTest(properties = {"MAIL_HOST=localhost", "MAIL_USER=test", "MAIL_PASSWORD=test"})
@Testcontainers
class PortfolioApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("pgvector/pgvector:pg17");

    @Test
    void contextLoads() {
        
    }
}
