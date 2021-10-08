package com.masa.paky.machinery;

import com.masa.paky.machinery.entity.Machinery;
import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.machinery.exception.MachineryNotFoundException;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MachineryHandler {
  private final MachineryRepository repository;
  private final VendorRepository vendorRepository;

  public Machinery create(String description) {
    Machinery machinery = getNewOneWith(description);
    repository.save(machinery);
    return machinery;
  }

  private Machinery getNewOneWith(String description) {
    Machinery machinery = new Machinery();
    machinery.setMachineryId(UUID.randomUUID().toString());
    machinery.setDescription(description);
    return machinery;
  }

  public void assign(String machineryId, String vendorId) {
    checkVendorExists(vendorId);
    bind(machineryId, vendorId);
  }

  private void bind(String machineryId, String vendorId) {
    repository
        .findById(machineryId)
        .ifPresentOrElse($ -> setVendor($, vendorId), () -> raiseError(machineryId));
  }

  private void raiseError(String machineryId) {
    throw new MachineryNotFoundException(machineryId);
  }

  private void checkVendorExists(String vendorId) {
    if (!vendorRepository.findById(vendorId).isPresent())
      throw new VendorNotFoundException(vendorId);
  }

  private void setVendor(Machinery machinery, String vendorId) {
    machinery.setVendorId(vendorId);
    repository.update(machinery);
  }
}
