package com.capital11.it;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * MySQL for the integration tests: same image as docker-compose.yml, initialised with the real schema so
 * Hibernate's ddl-auto=validate checks the entities against it. One container is shared by every test class
 * that uses the same (cached) Spring context.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.4"))
                .withCopyFileToContainer(MountableFile.forHostPath("docker/mysql/init/01-schema.sql"),
                        "/docker-entrypoint-initdb.d/01-schema.sql");
    }
}
