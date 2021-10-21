package com.masa.integration.paky.endpoint;


import static io.micronaut.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;
import com.masa.endpoint.paky.beans.*;
import com.masa.paky.paky.entity.Paky;
import io.micronaut.http.HttpResponse;
import java.util.Optional;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;


class PakyControllerIT extends ControllerBase {
  @Inject
  PakyActions pakyActions;

  @Inject
  MachineryActions machineryActions;

  @Inject
  BaseActions baseActions;

  public PakyControllerIT() {
    openMocks(this);
  }

  @Test
  void register_createNewPakyWithEndPoint_response200() {
    final HttpResponse<NewPaky> result =
            pakyActions.registerPaky();
    assertEquals(OK.getCode(), result.getStatus().getCode());
    assertTrue(result.getBody().isPresent());
  }



  @Test
  void registerPaky_searchReturnedId_returnthePaky() {
    final Optional<NewPaky> expectedSummary = pakyActions.givenAPaky();
    final HttpResponse<Paky> searchResponse = pakyActions.search(expectedSummary.get().getIdPaky());
    assertEquals(OK.getCode(), searchResponse.getStatus().getCode());
    assertTrue(searchResponse.getBody().isPresent());
    final Paky result = searchResponse.getBody().get();
    assertEquals(expectedSummary.get().getIdPaky(), result.getIdPaky());
    assertEquals(expectedSummary.get().getTopic(), result.getTopic());
  }

  @Test
  void registerPaky_bookPakyForCustomer_wellDone() {
    final Optional<NewPaky> expectedSummary = pakyActions.givenAPaky();
    pakyActions.check(expectedSummary);
    final NewPaky target = expectedSummary.get();
    final HttpResponse<PakyAnswer> result = pakyActions.bookTo(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void sendPaky_bookPakyForCustomer_wellDone() {
    final NewPaky target = pakyActions.aGivenPakyBookedByAVendor();
    final HttpResponse<PakyAnswer> result = pakyActions.sendTo(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void receivePaky_bookPakyForCustomer_wellDone() {
    final NewPaky target = pakyActions.agivenPakySentToVendor();
    final HttpResponse<PakyAnswer> result = pakyActions.receive(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void destinatePaky_bookPakyForCustomer_wellDone() {
    final NewPaky target = pakyActions.aPakyReceivedByVendor();
    final HttpResponse<PakyAnswer> result = pakyActions.destinate(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void deployPaky_bookPakyForCustomer_wellDone() {
    final NewPaky target = pakyActions.aPakyDestinateToACustomer();
    final HttpResponse<PakyAnswer> result = pakyActions.deploy(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void plugPaky_bookPakyForCustomer_wellDone() {
    final NewPaky target = pakyActions.aPakySentToACustomer();
    final HttpResponse<PakyAnswer> result = pakyActions.plug(target);
    assertEquals(OK.getCode(), result.status().getCode());
  }

  @Test
  void pluggedPaky_report_statusUpdate() {
    final NewPaky paky = pakyActions.aPluggedPaky();
    HttpResponse<PakyAnswer> result = pakyActions.postCommand(BASE_PAKY_URL + paky.getIdPaky() + "/report",new ReportCommand(10));
    assertEquals(OK.getCode(), result.status().getCode());
    assertTrue(result.getBody().isPresent());
  }


}
