package com.masa.paky.paky;

import com.masa.paky.Addressable;
import com.masa.paky.AddressableFinder;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotInTransitException;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import static com.masa.paky.paky.entity.PakyStatus.DELIVERED;
import static com.masa.paky.paky.entity.PakyStatus.INTRANSIT;

@RequiredArgsConstructor
public class PakyExpeditionManager<T extends Addressable, I extends Serializable> {
    public static final String RECEIVE = "receive";
    private final AddressableFinder<T, I> repository;
    private final PakyRepository pakyRepository;


    public void send(String pakyId, I vendorId) {
        final Paky paky = getPaky(pakyId);
        checkVendor(vendorId);
        checkRightCustomer(vendorId, paky);
        expedit(paky);
        persist(paky);
    }

    private void checkVendor(I vendorId) {
        if (!repository.findById(vendorId).isPresent())
            throw new VendorNotFoundException(vendorId.toString());
    }

    private void checkRightCustomer(I vendorId, Paky paky) {
        if (!paky.getVendorId().equals(vendorId))
            throw new DestinationMissMatchException(paky.getVendorId(), vendorId.toString());
    }

    private void persist(Paky paky) {
        pakyRepository.update(paky);
    }

    private void expedit(Paky paky) {
        setStep(paky, INTRANSIT);
    }


    private Paky getPaky(String pakyId) {
        final Optional<Paky> paky = pakyRepository.findById(pakyId);
        if (paky.isPresent())
            return paky.get();
        else
            throw new PakyNotFoundException(pakyId);
    }


    public void receive(String pakyId, I vendorId) {
        final Paky paky = getPaky(pakyId);
        checkThatPakyWasSent(pakyId, paky);
        checkVendor(vendorId);
        checkRightCustomer(vendorId, paky);
        deliver(paky);
        persist(paky);
    }

    private void checkThatPakyWasSent(String pakyId, Paky paky) {
        if (isInTransit(paky))
            throw new PakyNotInTransitException(pakyId, paky.getStep().name(), RECEIVE);
    }

    private boolean isInTransit(Paky paky) {
        return !paky.getStep().equals(INTRANSIT);
    }

    private void deliver(Paky paky) {
        setStep(paky, DELIVERED);
    }

    private void setStep(Paky paky, PakyStatus step) {
        paky.setStep(step);
        paky.setLastAction(new Date());
    }

}