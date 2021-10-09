package com.masa.paky.paky.expedition;

import com.masa.paky.AddressableFinder;
import com.masa.paky.customer.entity.Customer;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;

import static com.masa.paky.paky.entity.ErrorStatus.*;

public class CustomerExpeditionManager extends ExpeditionManager<Customer, String> {
    public CustomerExpeditionManager(AddressableFinder<Customer, String> repository, PakyRepository pakyRepository) {
        super(repository, pakyRepository);
    }

    @Override
    protected boolean isRightRecipient(String recipientId, Paky paky) {
        return !paky.getCustomerId().equals(recipientId);
    }

    @Override
    protected int getUnidentifiedRecipiente() {
        return RECEIVED_BY_UNIDENTIFIED_CUSTOMER;
    }

    @Override
    protected void raiseDestinationMissMatch(String recipientId, Paky paky) {
        throw new DestinationMissMatchException(paky.getVendorId(), recipientId);
    }

    @Override
    protected int getSentToWrongRecipientCode() {
        return SENT_TO_WRONG_CUSTOMER;
    }

    @Override
    protected void raiseRecipientNotFound(String recipientId) {
        throw new CustomerNotFoundException(recipientId);
    }

    @Override
    protected int getWrongRecipientCode() {
        return RECEIVED_BY_WRONG_CUSTOMER;
    }

    @Override
    protected void receiveAnyWay(String recipientId, Paky paky) {
        paky.setCustomerId(recipientId);
    }
}
