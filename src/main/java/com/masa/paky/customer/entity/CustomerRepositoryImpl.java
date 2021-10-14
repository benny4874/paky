package com.masa.paky.customer.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import java.util.Optional;
import javax.persistence.EntityManager;

@Singleton
public class CustomerRepositoryImpl implements CustomerRepository {

  private final EntityManager entityManager;

  public CustomerRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @ReadOnly
  public Optional<Customer> findById(String customerId) {
    return Optional.ofNullable(entityManager.find(Customer.class, customerId));
  }
}
