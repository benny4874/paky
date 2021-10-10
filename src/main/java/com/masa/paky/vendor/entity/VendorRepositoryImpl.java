package com.masa.paky.vendor.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import java.util.Optional;
import javax.persistence.EntityManager;

@Singleton
public class VendorRepositoryImpl implements VendorRepository {

  private final EntityManager entityManager;

  public VendorRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @ReadOnly
  public Optional<Vendor> findById(String vendorId) {
    return Optional.ofNullable(entityManager.find(Vendor.class, vendorId));
  }
}
