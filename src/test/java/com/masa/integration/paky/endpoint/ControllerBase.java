package com.masa.integration.paky.endpoint;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerBase {
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static final String CLEANUP_VENDOR = "delete from VENDOR";
    public static final String CLEANUP_PAKY = "delete from PAKY";
    public static final String CLEANUP_MACHINERY = "delete from MACHINERY";
    public static final String CLEANUP_RECIPE = "delete from RECIPE";
    public static final String CLEANUP_CUSTOMER = "delete from CUSTOMER";
    public static final JdbcDatabaseContainer db =
            (JdbcDatabaseContainer) new MySQLContainer("mysql:latest").withExposedPorts(3306, 3306);

    @Inject
    @Client("/")
    HttpClient client;

    @SneakyThrows
    public ControllerBase() {
        db.start();
        Connection connection = getJdbcConnection();
        Database database =
                DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase =
                new Liquibase(
                        "com/masa/paky/db/db.changelog-master.xml",
                        new ClassLoaderResourceAccessor(),
                        database);
        liquibase.update(new Contexts());
        System.setProperty("datasources.default.url", db.getJdbcUrl());
        System.setProperty("datasources.default.username", db.getUsername());
        System.setProperty("datasources.default.password", db.getPassword());
        System.setProperty("datasources.default.driverClassName", JDBC_DRIVER);
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        Class.forName(JDBC_DRIVER);
        final Connection connection =
                DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        execute(connection, CLEANUP_RECIPE);
        execute(connection, CLEANUP_MACHINERY);
        execute(connection, CLEANUP_PAKY);
        execute(connection, CLEANUP_VENDOR);
        execute(connection, CLEANUP_CUSTOMER);
        connection.close();
    }


    @SneakyThrows
    protected void givenExistsVendor() {
        final Connection connection = getJdbcConnection();
        execute(connection, "insert into VENDOR (VENDOR_ID,ID_SSO) values ('vendor1','vendor1')");
        connection.close();
    }

    @SneakyThrows
    protected void givenExistsCustomer() {
        final Connection connection = getJdbcConnection();
        execute(connection, "insert into CUSTOMER (CUSTOMER_ID,ID_SSO) values ('customer1','customer1')");
        connection.close();
    }

    protected Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    private void execute(Connection connection, String cleanupVendor) throws SQLException {
        final PreparedStatement deleteVendor = connection.prepareStatement(cleanupVendor);
        deleteVendor.executeUpdate();
    }
}
