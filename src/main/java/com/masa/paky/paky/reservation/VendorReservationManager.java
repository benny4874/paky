package com.masa.paky.paky.reservation;

import com.masa.paky.exceptions.SubjectNotFoundException;
import com.masa.paky.paky.PakyLifeCycleHandler;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;

public class VendorReservationManager extends ReservationManager<Vendor, String, VendorRepository> {

  public VendorReservationManager(
      PakyRepository pakyRepository, VendorRepository subjectRepository) {
    super(subjectRepository, pakyRepository);
  }

  @Override
  protected SubjectNotFoundException raiseSubjectNotFound(String subjectId) {
    return new VendorNotFoundException(subjectId);
  }

  @Override
  protected void assign(String subjectId, PakyLifeCycleHandler lifeCycleHandler) {
    lifeCycleHandler.bookFor(subjectId);
  }
}
