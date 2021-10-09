package com.masa.paky.customer.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;

import javax.persistence.EntityManager;
import java.util.Optional;

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
