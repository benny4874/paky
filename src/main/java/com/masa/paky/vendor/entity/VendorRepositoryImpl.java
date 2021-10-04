package com.masa.paky.vendor.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;

import javax.persistence.EntityManager;
import java.util.Optional;

@Singleton
public class VendorRepositoryImpl implements VendorRepository {

    private final EntityManager entityManager;

    public VendorRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @ReadOnly
    public Optional<Vendor> findById(String customerId) {
        return Optional.ofNullable(entityManager.find(Vendor.class, customerId));
    }


}
