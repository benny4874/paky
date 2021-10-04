package com.masa.paky.paky.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;

import javax.persistence.EntityManager;
import java.util.Optional;

@Singleton
public class PakyRepoositoryImpl implements PakyRepository{

    private final EntityManager entityManager;

    public PakyRepoositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @TransactionalAdvice
    public Paky save(Paky paky) {
        entityManager.persist(paky);
        return paky;
    }

    @Override
    @TransactionalAdvice
    public void update(Paky paky) {
        entityManager.merge(paky);
    }

    @Override
    @ReadOnly
    public Optional<Paky> findById(String id) {
        return Optional.ofNullable(entityManager.find(Paky.class,id));
    }
}
