package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import java.util.Optional;

public class PakyReservationManager {
  private final PakyRepository pakyRepository;
  private final VendorRepository vendorRepository;
  private final PakyLifeCycleHandlerFactory lifeCycleHandlerFactory;

  public PakyReservationManager(PakyRepository pakyRepository, VendorRepository vendorRepository) {
    this.pakyRepository = pakyRepository;
    this.vendorRepository = vendorRepository;
    this.lifeCycleHandlerFactory = new PakyLifeCycleHandlerFactory(pakyRepository);
  }

  public void reserve(String pakyId, String vendorId) {
    exists(vendorId);
    pakyRepository
        .findById(pakyId)
        .ifPresentOrElse(
            paky -> assignToVendor(paky, vendorId),
            () -> {
              throw new PakyNotFoundException(pakyId);
            });
  }

  private void exists(String vendorId) {
    final Optional<Vendor> vendor = vendorRepository.findById(vendorId);
    if (!vendor.isPresent()) throw new VendorNotFoundException(vendorId);
  }

  private void assignToVendor(Paky paky, String vendorId) {
    final PakyLifeCycleHandler lifeCycleHandler = lifeCycleHandlerFactory.getFor(paky);
    lifeCycleHandler.bookFor(vendorId);
  }
}
