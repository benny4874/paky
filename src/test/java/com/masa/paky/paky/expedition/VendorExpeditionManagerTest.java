package com.masa.paky.paky.expedition;

import static com.masa.paky.paky.entity.ErrorStatus.RECEIVED_BUT_NEVER_SENT;
import static com.masa.paky.paky.entity.ErrorStatus.SENT_TO_WRONG_VENDOR;
import static com.masa.paky.paky.entity.PakyStatus.*;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.masa.paky.AddressableFinder;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotInTransitException;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class VendorExpeditionManagerTest {
  @Mock AddressableFinder<Vendor, String> repository;
  @Mock PakyRepository pakyRepository;

  @Mock Vendor recipient;

  VendorExpeditionManager underTest;

  public VendorExpeditionManagerTest() {
    MockitoAnnotations.openMocks(this);
    underTest = new VendorExpeditionManager(repository, pakyRepository);
    when(repository.findById("aVendor")).thenReturn(Optional.of(recipient));
    when(repository.findById("anOtherVendor")).thenReturn(Optional.of(recipient));
    when(repository.findById(eq("aVendorThatNotExists"))).thenReturn(Optional.empty());
  }

  @Test
  void paky_sentToCustomer_StepIntransit() {
    Paky fixture = agivenPakyToSend();
    underTest.send(fixture.getIdPaky(), "aVendor");
    assertEquals(INTRANSIT, fixture.getStep());
    verify(pakyRepository).update(fixture);
  }

  @Test
  void paky_sentToWrongCustomer_ThrowDestinationMissMatchException() {
    Paky fixture = agivenPakyToSend();
    fixture.setVendorId("anotherVendor");
    assertThrows(
        DestinationMissMatchException.class, () -> underTest.send(fixture.getIdPaky(), "aVendor"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"aVendorThatNotExists"})
  void paky_sentNotExistingVendor_ThrowVendorNotFoundException(String notExistingVendorId) {
    Paky fixture = agivenPakyToSend();
    fixture.setVendorId(notExistingVendorId);
    assertThrows(
        VendorNotFoundException.class,
        () -> underTest.send(fixture.getIdPaky(), "aVendorThatNotExists"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"pakyThatNotExists"})
  void send_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId) {
    assertThrows(PakyNotFoundException.class, () -> underTest.send(notExistingPAkyId, "aVendor"));
  }

  @ParameterizedTest(name = "{0} sent to {2}")
  @CsvSource({"aPaky,aVendorThatNotExists,aVendorThatNotExists", "aPaky,aVendor,anotherVendor"})
  void expeditionFail_StepNotChange_pakyNotSaved(
      String pakyId, String expectedVendor, String realVendor) {
    Paky fixture = agivenPakyToSend();
    fixture.setVendorId(expectedVendor);
    PakyStatus originalStatus = fixture.getStep();
    send(pakyId, realVendor);
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(originalStatus, fixture.getStep()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()),
        () -> assertEquals(SENT_TO_WRONG_VENDOR, fixture.getErrorCode()));
  }

  @Test
  void aPakyThatNotExists_is_sent_nothingIsSaved() {
    Paky fixture = agivenPakyToSend();
    fixture.setIdPaky("aPakyThatNotExists");
    send("aPakyThatNotExists", "aVendor");
    verify(pakyRepository, never()).update(fixture);
  }

  @Test
  void paky_receiveCustomer_StepReceived() {
    Paky fixture = agivenPakySent();
    underTest.receive(fixture.getIdPaky(), "aVendor");
    assertEquals(DELIVERED, fixture.getStep());
    verify(pakyRepository).update(fixture);
  }

  @Test
  void paky_receivedbyWrongCustomer_ThrowDestinationMissMatchException() {
    Paky fixture = agivenPakySent();
    fixture.setVendorId("anotherVendor");
    assertThrows(
        DestinationMissMatchException.class,
        () -> underTest.receive(fixture.getIdPaky(), "aVendor"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"aVendorThatNotExists"})
  void paky_receivedNotExistingCustomer_ThrowVendorNotFoundException(String notExistingVendorId) {
    Paky fixture = agivenPakySent();
    fixture.setVendorId(notExistingVendorId);
    assertThrows(
        VendorNotFoundException.class,
        () -> underTest.receive(fixture.getIdPaky(), "aVendorThatNotExists"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"pakyThatNotExists"})
  void receive_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId) {
    assertThrows(
        PakyNotFoundException.class, () -> underTest.receive(notExistingPAkyId, "aVendor"));
  }

  @ParameterizedTest(name = "{0} for {1} sent to {2}")
  @CsvSource({"aPakyThatNotExists,aVendor,aVendor"})
  void receiveFail_StepNotChange_pakyNotSaved(
      String pakyId, String expectedVendor, String realVendor) {
    Paky fixture = agivenPakySent();
    fixture.setVendorId(expectedVendor);
    PakyStatus originalStatus = fixture.getStep();
    receive(pakyId, realVendor);
    verifyNoActionIsTaken(fixture, originalStatus);
  }

  @ParameterizedTest(name = "{0} for {1} sent to {2}")
  @CsvSource({
    "aPaky,aVendorThatNotExists,aVendor,100",
    "aPaky,aVendor,aVendorThatNotExists,400",
    "aPaky,aVendor,anOtherVendor,100"
  })
  void receiveAdifferentVendor_StepReceived_pakyUpdated_StatusError(
      String pakyId, String expectedVendor, String realVendor, int erroprCode) {
    Paky fixture = agivenPakySent();
    fixture.setVendorId(expectedVendor);
    receive(pakyId, realVendor);
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(DELIVERED, fixture.getStep()),
        () -> assertEquals(realVendor, fixture.getVendorId()),
        () -> assertEquals(erroprCode, fixture.getErrorCode()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()));
  }

  @Test
  void receive_pakyNotInTransit_pakyNotInTransitException_traciabilityError_Error200() {
    Paky fixture = agivenPakyToSend();
    fixture.setStep(CREATED);
    assertThrows(
        PakyNotInTransitException.class,
        () -> underTest.receive(fixture.getIdPaky(), fixture.getVendorId()));
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(DELIVERED, fixture.getStep()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()),
        () -> assertEquals(RECEIVED_BUT_NEVER_SENT, fixture.getErrorCode()));
  }

  private void verifyNoActionIsTaken(Paky fixture, PakyStatus originalStatus) {
    verify(pakyRepository, never()).update(fixture);
    assertEquals(originalStatus, fixture.getStep());
  }

  private void receive(String pakyId, String realVendor) {
    try {
      underTest.receive(pakyId, realVendor);
    } catch (Exception e) {
      // do Nothing
    }
  }

  private Paky agivenPakySent() {
    final Paky paky = agivenPakyToSend();
    paky.setStep(INTRANSIT);
    return paky;
  }

  private void send(String pakyId, String realVendor) {
    try {
      underTest.send(pakyId, realVendor);
    } catch (Exception e) {
      // do Nothing
    }
  }

  private Paky agivenPakyToSend() {
    Paky fixture = new Paky();
    fixture.setIdPaky("aPaky");
    fixture.setVendorId("aVendor");
    fixture.setStep(ASSIGNED);
    makeSearchable(fixture);
    return fixture;
  }

  private void makeSearchable(Paky fixture) {
    when(pakyRepository.findById("aPaky")).thenReturn(Optional.of(fixture));
  }
}
