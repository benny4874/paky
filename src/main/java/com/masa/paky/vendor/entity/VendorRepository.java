package com.masa.paky.vendor.entity;

import com.masa.paky.AddressableFinder;
import java.util.Optional;

public interface VendorRepository extends AddressableFinder<Vendor, String> {
  Optional<Vendor> findById(String customerId);
}
