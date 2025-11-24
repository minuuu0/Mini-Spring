package org.example.minispring.container.testdata;

public class UserDao {
    private final DatabaseConnection connection;

    public UserDao(DatabaseConnection connection) {
        this.connection = connection;
    }

    public DatabaseConnection getConnection() {
        return connection;
    }
}
