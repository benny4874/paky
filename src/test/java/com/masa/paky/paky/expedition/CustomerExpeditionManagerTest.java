package com.masa.paky.paky.expedition;

import static com.masa.paky.paky.entity.ErrorStatus.RECEIVED_BUT_NEVER_SENT;
import static com.masa.paky.paky.entity.ErrorStatus.SENT_TO_WRONG_CUSTOMER;
import static com.masa.paky.paky.entity.PakyStatus.*;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.masa.paky.AddressableFinder;
import com.masa.paky.customer.entity.Customer;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotInTransitException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CustomerExpeditionManagerTest {
  @Mock AddressableFinder<Customer, String> repository;
  @Mock PakyRepository pakyRepository;

  @Mock Customer recipient;

  CustomerExpeditionManager underTest;

  public CustomerExpeditionManagerTest() {
    MockitoAnnotations.openMocks(this);
    underTest = new CustomerExpeditionManager(repository, pakyRepository);
    when(repository.findById("aCustomer")).thenReturn(Optional.of(recipient));
    when(repository.findById("anotherCustomer")).thenReturn(Optional.of(recipient));
    when(repository.findById(eq("aCustomerThatNotExists"))).thenReturn(Optional.empty());
  }

  @Test
  void paky_sentToCustomer_StepIntransit() {
    Paky fixture = agivenPakyToSend();
    underTest.send(fixture.getIdPaky(), "aCustomer");
    assertEquals(INTRANSIT, fixture.getStep());
    verify(pakyRepository).update(fixture);
  }

  @Test
  void paky_sentToWrongCustomer_ThrowDestinationMissMatchException() {
    Paky fixture = agivenPakyToSend();
    fixture.setCustomerId("anotherCustomer");
    assertThrows(
        DestinationMissMatchException.class,
        () -> underTest.send(fixture.getIdPaky(), "aCustomer"));
  }

  @ParameterizedTest(name = "input = {0}")
  @NullAndEmptySource
  @ValueSource(strings = {"aCustomerThatNotExists"})
  void paky_sentNotExistingCustomer_ThrowCustomerNotFoundException(String notExistingCustomerId) {
    Paky fixture = agivenPakyToSend();
    fixture.setCustomerId(notExistingCustomerId);
    assertThrows(
        CustomerNotFoundException.class,
        () -> underTest.send(fixture.getIdPaky(), "aCustomerThatNotExists"));
  }

  @ParameterizedTest(name = "pakyId = {0} throw PakyNotFounException")
  @NullAndEmptySource
  @ValueSource(strings = {"pakyThatNotExists"})
  void send_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId) {
    assertThrows(PakyNotFoundException.class, () -> underTest.send(notExistingPAkyId, "aCustomer"));
  }

  @ParameterizedTest(name = "{0} sent to {2}")
  @CsvSource({
    "aPaky,aCustomerThatNotExists,aCustomerThatNotExists",
    "aPaky,aCustomer,anotherCustomer"
  })
  void expeditionFail_StepNotChange_pakyNotSaved(
      String pakyId, String expectedCustomer, String realCustomer) {
    Paky fixture = agivenPakyToSend();
    fixture.setCustomerId(expectedCustomer);
    PakyStatus originalStatus = fixture.getStep();
    send(pakyId, realCustomer);
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(originalStatus, fixture.getStep()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()),
        () -> assertEquals(SENT_TO_WRONG_CUSTOMER, fixture.getErrorCode()));
  }

  @Test
  void aPakyThatNotExists_is_sent_nothingIsSaved() {
    Paky fixture = agivenPakyToSend();
    fixture.setIdPaky("aPakyThatNotExists");
    send("aPakyThatNotExists", "aCustomer");
    verify(pakyRepository, never()).update(fixture);
  }

  @Test
  void paky_receiveCustomer_StepReceived() {
    Paky fixture = agivenPakySent();
    underTest.receive(fixture.getIdPaky(), "aCustomer");
    assertEquals(OPERATING, fixture.getStep());
    verify(pakyRepository).update(fixture);
  }

  @Test
  void paky_receivedbyWrongCustomer_ThrowDestinationMissMatchException() {
    Paky fixture = agivenPakySent();
    fixture.setCustomerId("anotherCustomer");
    assertThrows(
        DestinationMissMatchException.class,
        () -> underTest.receive(fixture.getIdPaky(), "aCustomer"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"aCustomerThatNotExists"})
  void paky_receivedNotExistingCustomer_ThrowCustomerNotFoundException(
      String notExistingCustomerId) {
    Paky fixture = agivenPakySent();
    fixture.setCustomerId(notExistingCustomerId);
    assertThrows(
        CustomerNotFoundException.class,
        () -> underTest.receive(fixture.getIdPaky(), "aCustomerThatNotExists"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"pakyThatNotExists"})
  void receive_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId) {
    assertThrows(
        PakyNotFoundException.class, () -> underTest.receive(notExistingPAkyId, "aCustomer"));
  }

  @ParameterizedTest(name = "{0} for {1} sent to {2}")
  @CsvSource({"aPakyThatNotExists,aCustomer,aCustomer"})
  void receiveFail_StepNotChange_pakyNotSaved(
      String pakyId, String expectedCustomer, String realCustomer) {
    Paky fixture = agivenPakySent();
    fixture.setCustomerId(expectedCustomer);
    PakyStatus originalStatus = fixture.getStep();
    receive(pakyId, realCustomer);
    verifyNoActionIsTaken(fixture, originalStatus);
  }

  @ParameterizedTest(name = "{0} for {1} sent to {2}")
  @CsvSource({
    "aPaky,aCustomerThatNotExists,aCustomer,700",
    "aPaky,aCustomer,aCustomerThatNotExists,800",
    "aPaky,aCustomer,anotherCustomer,700"
  })
  void receiveAdifferentCustomer_StepReceived_pakyUpdated_StatusError(
      String pakyId, String expectedCustomer, String realCustomer, int erroprCode) {
    Paky fixture = agivenPakySent();
    fixture.setCustomerId(expectedCustomer);
    receive(pakyId, realCustomer);
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(OPERATING, fixture.getStep()),
        () -> assertEquals(realCustomer, fixture.getCustomerId()),
        () -> assertEquals(erroprCode, fixture.getErrorCode()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()));
  }

  @Test
  void receive_pakyNotInTransit_pakyNotInTransitException_traciabilityError_Error200() {
    Paky fixture = agivenPakyToSend();
    fixture.setStep(CREATED);
    assertThrows(
        PakyNotInTransitException.class,
        () -> underTest.receive(fixture.getIdPaky(), fixture.getCustomerId()));
    verify(pakyRepository).update(fixture);
    assertAll(
        () -> assertEquals(OPERATING, fixture.getStep()),
        () -> assertEquals(ERROR, fixture.getTraciabilityStatus()),
        () -> assertEquals(RECEIVED_BUT_NEVER_SENT, fixture.getErrorCode()));
  }

  private void verifyNoActionIsTaken(Paky fixture, PakyStatus originalStatus) {
    verify(pakyRepository, never()).update(fixture);
    assertEquals(originalStatus, fixture.getStep());
  }

  private void receive(String pakyId, String realCustomer) {
    try {
      underTest.receive(pakyId, realCustomer);
    } catch (Exception e) {
      // do Nothing
    }
  }

  private Paky agivenPakySent() {
    final Paky paky = agivenPakyToSend();
    paky.setStep(INTRANSIT);
    return paky;
  }

  private void send(String pakyId, String realCustomer) {
    try {
      underTest.send(pakyId, realCustomer);
    } catch (Exception e) {
      // do Nothing
    }
  }

  private Paky agivenPakyToSend() {
    Paky fixture = new Paky();
    fixture.setIdPaky("aPaky");
    fixture.setCustomerId("aCustomer");
    fixture.setStep(SOLD);
    makeSearchable(fixture);
    return fixture;
  }

  private void makeSearchable(Paky fixture) {
    when(pakyRepository.findById("aPaky")).thenReturn(Optional.of(fixture));
  }
}
