package com.masa.paky.paky;

import static com.masa.paky.paky.entity.PakyStatus.CREATED;
import static com.masa.paky.paky.entity.TraciabilityStatus.OK;

import com.masa.paky.paky.entity.ErrorStatus;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class PakyLifeCycleHandlerFactory {

  PakyRepository pakyRepository;

  public PakyLifeCycleHandler createNew() {
    Paky emptyPaky = new Paky();
    emptyPaky.setIdPaky(UUID.randomUUID().toString());
    emptyPaky.setStep(CREATED);
    emptyPaky.setQuantita(0f);
    emptyPaky.setQuantitaPct(0f);
    emptyPaky.setTopic(emptyPaky.getIdPaky() + "/communication");
    emptyPaky.setDateCreated(new Date());
    emptyPaky.setLastAction(new Date());
    emptyPaky.setTraciabilityStatus(OK);
    emptyPaky.setErrorCode(ErrorStatus.OK);
    pakyRepository.save(emptyPaky);
    return new PakyLifeCycleHandler(emptyPaky, pakyRepository, true);
  }

  public PakyLifeCycleHandler getFor(@NonNull Paky target) {
    return new PakyLifeCycleHandler(target, pakyRepository, false);
  }
}
