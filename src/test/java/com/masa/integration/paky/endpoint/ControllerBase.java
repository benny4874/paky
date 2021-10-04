package com.masa.integration.paky.endpoint;

import com.masa.endpoint.paky.beans.ExpeditionCommand;
import com.masa.endpoint.paky.beans.PakyAnswer;
import io.micronaut.http.HttpResponse;
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

import static io.micronaut.http.HttpRequest.POST;


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerBase {
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static final String CLEANUP_VENDOR = "delete from VENDOR";
    public final static JdbcDatabaseContainer db = (JdbcDatabaseContainer) new MySQLContainer("mysql:latest")
            .withExposedPorts(3306, 3306);
    @Inject
    @Client("/")
    HttpClient client;

    @SneakyThrows
    public ControllerBase() {
        db.start();
        Connection connection = getJdbcConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("com/masa/paky/db/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);
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
        final Connection connection = DriverManager.getConnection(
                db.getJdbcUrl(), db.getUsername(), db.getPassword());
        final PreparedStatement insert = connection.prepareStatement(CLEANUP_VENDOR);
        insert.executeUpdate();
        connection.close();
    }

    @SneakyThrows
    protected void givenExistsVendor() {
        final Connection connection = getJdbcConnection();
        final PreparedStatement insert = connection.prepareStatement("insert into VENDOR (VENDOR_ID,ID_SSO) values ('vendor1','" + "vendor1" + "')");
        insert.executeUpdate();
        connection.close();
    }

    protected Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(
                db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }


}