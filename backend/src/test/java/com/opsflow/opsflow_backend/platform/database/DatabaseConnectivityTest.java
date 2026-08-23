package com.opsflow.opsflow_backend.platform.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({ PostgreSqlTestConfiguration.class, SecurityTestConfiguration.class })
class DatabaseConnectivityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Test
    void configuredDatasourceCanExecuteQuery() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertEquals(1, result);
    }

    @Test
    void flywayAppliesBaselineMigrationBeforeTests() {
        MigrationInfo migrationInfo = flyway.info().current();

        assertNotNull(migrationInfo);
        assertEquals("1", migrationInfo.getVersion().getVersion());
    }

}
