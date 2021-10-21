package com.masa.paky.paky.expedition;

import static com.masa.paky.paky.entity.ErrorStatus.RECEIVED_BUT_NEVER_SENT;
import static com.masa.paky.paky.entity.PakyStatus.DELIVERED;
import static com.masa.paky.paky.entity.PakyStatus.INTRANSIT;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;

import com.masa.paky.Addressable;
import com.masa.paky.AddressableFinder;
import com.masa.paky.exceptions.SubjectNotFoundException;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotInTransitException;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ExpeditionManager<T extends Addressable, I extends Serializable> {
  public static final String RECEIVE = "receive";
  private final AddressableFinder<T, I> repository;
  private final PakyRepository pakyRepository;

  public void send(String pakyId, I recipientId) {
    final Paky paky = getPaky(pakyId);
    send(recipientId, paky);
  }

  private void send(I recipientId, Paky paky) {
    try {
      checkRecipient(recipientId);
      checkRightRecipient(recipientId, paky);
      expedit(paky);
    } catch (SubjectNotFoundException | DestinationMissMatchException error) {
      traceError(paky, getSentToWrongRecipientCode());
      throw error;
    } finally {
      persist(paky);
    }
  }

  protected abstract int getSentToWrongRecipientCode();

  private void traceError(Paky paky, int sentToWrongVendor) {
    paky.setTraciabilityStatus(ERROR);
    paky.setErrorCode(sentToWrongVendor);
  }

  private void checkRecipient(I recipientId) {
    if (!repository.findById(recipientId).isPresent()) raiseRecipientNotFound(recipientId);
  }

  protected abstract void raiseRecipientNotFound(I recipientId);

  private void checkRightRecipient(I recipientId, Paky paky) {
    if (isRightRecipient(recipientId, paky)) {
      traceError(paky, getWrongRecipientCode());
      raiseDestinationMissMatch(recipientId, paky);
    }
  }

  protected abstract int getWrongRecipientCode();

  protected abstract void raiseDestinationMissMatch(I recipientId, Paky paky);

  protected abstract boolean isRightRecipient(I recipientId, Paky paky);

  protected abstract void receiveAnyWay(I recipientId, Paky paky);

  private void persist(Paky paky) {
    pakyRepository.update(paky);
  }

  private void expedit(Paky paky) {
    setStep(paky, INTRANSIT);
  }

  private Paky getPaky(String pakyId) {
    final Optional<Paky> paky = pakyRepository.findById(pakyId);
    if (paky.isPresent()) return paky.get();
    else throw new PakyNotFoundException(pakyId);
  }

  public void receive(String pakyId, I recipientId) {
    final Paky paky = getPaky(pakyId);
    receive(recipientId, paky);
  }

  private void receive(I recipientId, Paky paky) {
    try {
      checkThatPakyWasSent(paky.getIdPaky(), paky);
      checkRecipient(recipientId);
      checkRightRecipient(recipientId, paky);
    } catch (SubjectNotFoundException subjectNotFound) {
      traceError(paky, getUnidentifiedRecipiente());
      throw subjectNotFound;
    } finally {
      receiveAnyWay(recipientId, paky);
      deliver(paky);
      persist(paky);
    }
  }

  protected abstract int getUnidentifiedRecipiente();

  private void checkThatPakyWasSent(String pakyId, Paky paky) {
    if (isInTransit(paky)) {
      traceError(paky, RECEIVED_BUT_NEVER_SENT);
      throw new PakyNotInTransitException(pakyId, paky.getStep().name(), RECEIVE);
    }
  }

  private boolean isInTransit(Paky paky) {
    return !paky.getStep().equals(INTRANSIT);
  }

  private void deliver(Paky paky) {
    setStep(paky, getDeliveredStatus());
  }

  protected abstract PakyStatus getDeliveredStatus();

  private void setStep(Paky paky, PakyStatus step) {
    paky.setStep(step);
    paky.setLastAction(new Date());
  }
}
