package com.opsflow.opsflow_backend.platform.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class DatabaseConnectionVerifierTest {

    @Test
    void runCompletesWhenDatabaseConnectionIsValid() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        DatabaseConnectionVerifier verifier = new DatabaseConnectionVerifier(dataSource);

        assertDoesNotThrow(() -> verifier.run(arguments));

        verify(dataSource).getConnection();
        verify(connection).isValid(3);
        verify(connection).close();
    }

    @Test
    void runThrowsExceptionWhenDatabaseConnectionIsInvalid() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(false);
        DatabaseConnectionVerifier verifier = new DatabaseConnectionVerifier(dataSource);
        String expectedMessage = "The database connection could not be validated.";

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> verifier.run(arguments));

        String actualMessage = exception.getMessage();

        verify(dataSource).getConnection();
        verify(connection).isValid(3);
        verify(connection).close();
        assertEquals(expectedMessage, actualMessage);

    }

}
