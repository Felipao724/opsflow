package com.opsflow.opsflow_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({ PostgreSqlTestConfiguration.class, SecurityTestConfiguration.class })
class OpsflowBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
