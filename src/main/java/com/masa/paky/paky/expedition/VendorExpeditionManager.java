package com.masa.paky.paky.expedition;

import com.masa.paky.AddressableFinder;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;

import static com.masa.paky.paky.entity.ErrorStatus.*;


public class VendorExpeditionManager extends ExpeditionManager<Vendor, String> {

  public VendorExpeditionManager(AddressableFinder<Vendor, String> repository, PakyRepository pakyRepository) {
    super(repository, pakyRepository);
  }

  @Override
  protected boolean isRightRecipient(String recipientId, Paky paky) {
      return !paky.getVendorId().equals(recipientId);
  }

    @Override
    protected int getUnidentifiedRecipiente() {
        return RECEIVED_BY_UNIDENTIFIED_VENDOR;
    }

    @Override
  protected void raiseDestinationMissMatch(String recipientId, Paky paky) {
      throw new DestinationMissMatchException(paky.getCustomerId(), recipientId);
  }

    @Override
    protected int getSentToWrongRecipientCode() {
        return SENT_TO_WRONG_VENDOR;
    }

    @Override
  protected void raiseRecipientNotFound(String recipientId) {
      throw new VendorNotFoundException(recipientId);
  }

    @Override
    protected int getWrongRecipientCode() {
        return RECEIVED_BY_WRONG_VENDOR;
    }

    @Override
    protected void receiveAnyWay(String recipientId, Paky paky) {
        paky.setVendorId(recipientId);
    }
}
