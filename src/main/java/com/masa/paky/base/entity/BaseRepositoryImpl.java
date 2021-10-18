package com.masa.paky.base.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;

import javax.persistence.EntityManager;
import java.util.Optional;

@Singleton
public class BaseRepositoryImpl implements BaseRepository {

    private final EntityManager entityManager;

    public BaseRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @ReadOnly
    public Optional<Base> findById(String id) {
        return Optional.ofNullable(entityManager.find(Base.class, id));
    }

    @Override
    @TransactionalAdvice
    public void save(Base base) {
        entityManager.persist(base);
    }

    @Override
    @TransactionalAdvice
    public void update(Base base) {
        entityManager.merge(base);
    }
}
