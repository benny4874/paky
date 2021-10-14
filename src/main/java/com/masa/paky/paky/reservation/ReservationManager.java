package com.masa.paky.paky.reservation;

import com.masa.paky.Addressable;
import com.masa.paky.AddressableFinder;
import com.masa.paky.exceptions.SubjectNotFoundException;
import com.masa.paky.paky.PakyLifeCycleHandler;
import com.masa.paky.paky.PakyLifeCycleHandlerFactory;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import java.io.Serializable;

public abstract class ReservationManager<
    T extends Addressable, I extends Serializable, K extends AddressableFinder<T, I>> {
  protected final K subjectRepository;
  protected final PakyRepository pakyRepository;
  protected final PakyLifeCycleHandlerFactory lifeCycleHandlerFactory;

  public ReservationManager(K subjectRepository, PakyRepository pakyRepository) {
    this.subjectRepository = subjectRepository;
    this.pakyRepository = pakyRepository;
    this.lifeCycleHandlerFactory = new PakyLifeCycleHandlerFactory(pakyRepository);
  }

  public void reserve(String pakyId, I vendorId) {
    exists(vendorId);
    pakyRepository
        .findById(pakyId)
        .ifPresentOrElse(
            paky -> assignToSubject(paky, vendorId),
            () -> {
              throw new PakyNotFoundException(pakyId);
            });
  }

  protected void exists(I SubjectId) {
    subjectRepository.findById(SubjectId).orElseThrow(() -> raiseSubjectNotFound(SubjectId));
  }

  protected abstract SubjectNotFoundException raiseSubjectNotFound(I subjectId);

  protected void assignToSubject(Paky paky, I subjectId) {
    final PakyLifeCycleHandler lifeCycleHandler = lifeCycleHandlerFactory.getFor(paky);
    assign(subjectId, lifeCycleHandler);
  }

  protected abstract void assign(I subjectId, PakyLifeCycleHandler lifeCycleHandler);
}
