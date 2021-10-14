package com.masa.paky.customer.entity;

import com.masa.paky.AddressableFinder;
import java.util.Optional;

public interface CustomerRepository extends AddressableFinder<Customer, String> {
  Optional<Customer> findById(String customerId);
}
