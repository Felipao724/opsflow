package com.opsflow.opsflow_backend.platform.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
final class DatabaseConnectionVerifier implements ApplicationRunner {

    private final DataSource dataSource;

    DatabaseConnectionVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(3)) {
                throw new IllegalStateException("The database connection could not be validated.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to establish the database connection during startup. "
                            + "Verify the datasource configuration and database availability.",
                    exception);
        }
    }

}