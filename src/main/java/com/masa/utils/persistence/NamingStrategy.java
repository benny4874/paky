package com.masa.utils.persistence;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.data.hibernate.naming.DefaultPhysicalNamingStrategy;
import jakarta.inject.Singleton;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;


@Replaces(bean =  DefaultPhysicalNamingStrategy.class)
@Singleton
public class NamingStrategy implements PhysicalNamingStrategy {

    DefaultPhysicalNamingStrategy delegate = new DefaultPhysicalNamingStrategy();

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return delegate. toPhysicalCatalogName( identifier,  jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return delegate.toPhysicalSchemaName(identifier,jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        final Identifier regularIdentifier = delegate.toPhysicalTableName(identifier, jdbcEnvironment);
        return new Identifier(regularIdentifier.getText().toUpperCase(),regularIdentifier.isQuoted());
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        final Identifier regularIdentifier = delegate.toPhysicalSequenceName(identifier, jdbcEnvironment);
        return new Identifier(regularIdentifier.getText().toUpperCase(),regularIdentifier.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        final Identifier regularIdentifier = delegate.toPhysicalColumnName(identifier, jdbcEnvironment);
        return new Identifier(regularIdentifier.getText().toUpperCase(),regularIdentifier.isQuoted());

    }
}
