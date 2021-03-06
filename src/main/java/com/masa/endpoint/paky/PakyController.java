package com.masa.endpoint.paky;

import com.masa.endpoint.paky.beans.*;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import com.masa.paky.paky.PakyLifeCycleHandlerFactory;
import com.masa.paky.paky.PakyReporter;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotPluggedException;
import com.masa.paky.paky.expedition.CustomerExpeditionManager;
import com.masa.paky.paky.expedition.VendorExpeditionManager;
import com.masa.paky.paky.reservation.CustomerReservationManager;
import com.masa.paky.paky.reservation.VendorReservationManager;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.Optional;

import static io.micronaut.http.HttpResponse.badRequest;
import static io.micronaut.http.HttpResponse.ok;

@ExecuteOn(TaskExecutors.IO)
@Introspected
@Controller("/api/v1/paky")
public class PakyController {

  protected final PakyRepository pakyRepository;
  protected final VendorRepository vendorRepository;
  protected final CustomerRepository customerRepository;
  private final PakyLifeCycleHandlerFactory lifeCycleHandlerFactory;
  private final PakyReporter reporter;

  public PakyController(
      PakyRepository pakyRepository,
      VendorRepository vendorRepository,
      CustomerRepository customerRepository) {
    this.pakyRepository = pakyRepository;
    lifeCycleHandlerFactory = new PakyLifeCycleHandlerFactory(pakyRepository);
    this.vendorRepository = vendorRepository;
    this.customerRepository = customerRepository;
    reporter = new PakyReporter(pakyRepository);
  }

  @Get("register")
  HttpResponse<NewPaky> register() {
    final Paky paky = lifeCycleHandlerFactory.createNew().get();
    NewPaky response = new NewPaky(paky.getIdPaky(), paky.getTopic());
    return HttpResponse.status(HttpStatus.OK)
        .body(response)
        .contentType(MediaType.APPLICATION_JSON_TYPE);
  }

  @Get("{pakyId}")
  HttpResponse<Paky> getPaky(@PathVariable(value = "pakyId") String pakyId) {
    final Optional<Paky> paky = pakyRepository.findById(pakyId);
    if (paky.isPresent()) return ok(paky.get());
    else return HttpResponse.notFound();
  }

  @Post("{pakyId}/book")
  HttpResponse<PakyAnswer> bookPaky(
      @PathVariable(value = "pakyId") String pakyId, @Body ExpeditionCommand bookRequest) {

    try {
      VendorReservationManager reservationManager =
          new VendorReservationManager(pakyRepository, vendorRepository);
      reservationManager.reserve(pakyId, bookRequest.getVendorId());
      return ok(new PakyAnswer("Paky reserved for " + bookRequest.getVendorId()));
    } catch (PakyNotFoundException | VendorNotFoundException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/send")
  HttpResponse<PakyAnswer> sendPaky(
      @PathVariable(value = "pakyId") String pakyId, @Body ExpeditionCommand recipient) {
    try {
      VendorExpeditionManager vendorExpeditionManager =
          new VendorExpeditionManager(vendorRepository, pakyRepository);
      vendorExpeditionManager.send(pakyId, recipient.getVendorId());
      return ok(new PakyAnswer("Paky sent to " + recipient.getVendorId()));
    } catch (PakyNotFoundException
        | VendorNotFoundException
        | DestinationMissMatchException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/receive")
  HttpResponse<PakyAnswer> receivePaky(
      @PathVariable(value = "pakyId") String pakyId, @Body ExpeditionCommand recipient) {
    try {
      VendorExpeditionManager vendorExpeditionManager =
          new VendorExpeditionManager(vendorRepository, pakyRepository);
      vendorExpeditionManager.receive(pakyId, recipient.getVendorId());
      return ok(new PakyAnswer("Paky sent to " + recipient.getVendorId()));
    } catch (PakyNotFoundException
        | VendorNotFoundException
        | DestinationMissMatchException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/destinate")
  HttpResponse<PakyAnswer> destinatePaky(
      @PathVariable(value = "pakyId") String pakyId, @Body DestinationCommand bookRequest) {

    try {
      CustomerReservationManager reservationManager =
          new CustomerReservationManager(customerRepository, pakyRepository);
      reservationManager.reserve(pakyId, bookRequest.getCustomerId());
      return ok(new PakyAnswer("Paky reserved for " + bookRequest.getCustomerId()));
    } catch (PakyNotFoundException | CustomerNotFoundException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/deploy")
  HttpResponse<PakyAnswer> deployPaky(
      @PathVariable(value = "pakyId") String pakyId, @Body DestinationCommand recipient) {
    try {
      CustomerExpeditionManager customerExpeditionManager =
          new CustomerExpeditionManager(customerRepository, pakyRepository);
      customerExpeditionManager.send(pakyId, recipient.getCustomerId());
      return ok(new PakyAnswer("Paky sent to " + recipient.getCustomerId()));
    } catch (PakyNotFoundException
        | CustomerNotFoundException
        | DestinationMissMatchException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/plug")
  HttpResponse<PakyAnswer> plugPaky(
      @PathVariable(value = "pakyId") String pakyId, @Body DestinationCommand recipient) {
    try {
      CustomerExpeditionManager customerExpeditionManager =
          new CustomerExpeditionManager(customerRepository, pakyRepository);
      customerExpeditionManager.receive(pakyId, recipient.getCustomerId());
      return ok(new PakyAnswer("Paky sent to " + recipient.getCustomerId()));
    } catch (PakyNotFoundException
        | CustomerNotFoundException
        | DestinationMissMatchException wrongArgument) {
      return handleError(wrongArgument);
    }
  }

  @Post("{pakyId}/report")
  public  HttpResponse<PakyAnswer> report(@PathVariable(value = "pakyId") String pakyId, @Body ReportCommand report){
    try {
      reporter.report(pakyId, report.getQuantity());
      return ok(new PakyAnswer("report received"));
    } catch (PakyNotPluggedException notPlugged){
       return badRequest(new PakyAnswer(notPlugged.getMessage()));
    }
  }

  private HttpResponse<PakyAnswer> handleError(RuntimeException wrongArgument) {
    return HttpResponse.notFound(new PakyAnswer(wrongArgument.getMessage()));
  }
}
