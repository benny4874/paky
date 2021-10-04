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

import static com.masa.paky.paky.entity.ErrorStatus.*;
import static com.masa.paky.paky.entity.PakyStatus.DELIVERED;
import static com.masa.paky.paky.entity.PakyStatus.INTRANSIT;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;

@RequiredArgsConstructor
public class PakyExpeditionManager<T extends Addressable, I extends Serializable> {
    public static final String RECEIVE = "receive";
    private final AddressableFinder<T, I> repository;
    private final PakyRepository pakyRepository;


    public void send(String pakyId, I vendorId) {
        final Paky paky = getPaky(pakyId);
        send(vendorId, paky);

    }

    private void send(I vendorId, Paky paky) {
        try {
            checkVendor(vendorId);
            checkRightCustomer(vendorId, paky);
            expedit(paky);
        } catch (VendorNotFoundException | DestinationMissMatchException error){
            paky.setTraciabilityStatus(ERROR);
            paky.setErrorCode(SENT_TO_WRONG_VENDOR);
            throw error;
        }finally {
            persist(paky);
        }
    }

    private void checkVendor(I vendorId) {
        if (!repository.findById(vendorId).isPresent())
            throw new VendorNotFoundException(vendorId.toString());
    }

    private void checkRightCustomer(I vendorId, Paky paky) {
        if (!paky.getVendorId().equals(vendorId)) {
            paky.setTraciabilityStatus(ERROR);
            paky.setErrorCode(RECEIVED_BY_WRONG_VENDOR);
            throw new DestinationMissMatchException(paky.getVendorId(), vendorId.toString());
        }
    }

    private void receiveAnyWay(I vendorId, Paky paky) {
        paky.setVendorId(vendorId.toString());
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
        receive( vendorId, paky);
    }

    private void receive( I vendorId, Paky paky) {
        try {
            checkThatPakyWasSent(paky.getIdPaky(), paky);
            checkVendor(vendorId);
            checkRightCustomer(vendorId, paky);
        } catch(VendorNotFoundException vendorNotFound){
                paky.setTraciabilityStatus(ERROR);
                paky.setErrorCode(RECEIVED_BY_UNIDENTIFIED_VENDOR);
                throw vendorNotFound;
        }finally {
            receiveAnyWay(vendorId, paky);
            deliver(paky);
            persist(paky);
        }
    }

    private void checkThatPakyWasSent(String pakyId, Paky paky) {
        if (isInTransit(paky)) {
            paky.setTraciabilityStatus(ERROR);
            paky.setErrorCode(RECEIVED_BUT_NEVER_SENT);
            throw new PakyNotInTransitException(pakyId, paky.getStep().name(), RECEIVE);
        }
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
