package org.example.minispring.container.testdata;

import org.example.minispring.annotation.Bean;
import org.example.minispring.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection("jdbc:test://localhost:5432/testdb");
    }

    @Bean
    public UserDao userDao(DatabaseConnection connection) {
        return new UserDao(connection);
    }
}
