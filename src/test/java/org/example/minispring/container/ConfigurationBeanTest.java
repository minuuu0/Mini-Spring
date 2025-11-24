package org.example.minispring.container;

import org.example.minispring.container.testdata.DatabaseConnection;
import org.example.minispring.container.testdata.UserDao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationBeanTest {

    @Test
    void shouldRegisterBeanFromConfigurationClass() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        DatabaseConnection connection = context.getBean(DatabaseConnection.class);

        // Then
        assertNotNull(connection);
        assertEquals("jdbc:test://localhost:5432/testdb", connection.getUrl());
    }

    @Test
    void shouldUseBeanMethodNameAsBeanName() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        Object bean = context.getBean("databaseConnection");

        // Then
        assertNotNull(bean);
        assertInstanceOf(DatabaseConnection.class, bean);
    }

    @Test
    void shouldReturnSingletonForBeanMethod() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        DatabaseConnection bean1 = context.getBean(DatabaseConnection.class);
        DatabaseConnection bean2 = context.getBean(DatabaseConnection.class);

        // Then
        assertSame(bean1, bean2);
    }

    @Test
    void shouldInjectDependenciesIntoBeanMethod() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        UserDao userDao = context.getBean(UserDao.class);

        // Then
        assertNotNull(userDao);
        assertNotNull(userDao.getConnection());
        assertEquals("jdbc:test://localhost:5432/testdb", userDao.getConnection().getUrl());
    }
}
