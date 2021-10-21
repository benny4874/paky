package com.masa.integration.paky.endpoint;

import com.masa.endpoint.paky.beans.DestinationCommand;
import com.masa.endpoint.paky.beans.ExpeditionCommand;
import com.masa.endpoint.paky.beans.NewPaky;
import com.masa.endpoint.paky.beans.PakyAnswer;
import com.masa.paky.paky.entity.Paky;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.Serializable;
import java.util.Optional;

import static com.masa.integration.paky.endpoint.ControllerBase.*;
import static io.micronaut.http.HttpRequest.POST;
import static org.junit.Assert.fail;

@Singleton
class PakyActions {
    @Inject
    @Client("/")
    HttpClient client;



    public  HttpResponse<NewPaky> registerPaky() {
        return client.toBlocking().exchange(BASE_PAKY_URL + "register", NewPaky.class);
    }

    public  NewPaky aPluggedPaky() {
        final NewPaky target = aPakySentToACustomer();
        plug(target);
        return target;
    }

    public  NewPaky agivenPakySentToVendor() {
        final NewPaky target = aGivenPakyBookedByAVendor();
        sendTo(target);
        return target;
    }

    public  NewPaky aGivenPakyBookedByAVendor() {
        final Optional<NewPaky> expectedSummary = givenAPaky();
        check(expectedSummary);
        final NewPaky target = expectedSummary.get();
        bookTo(target);
        return target;
    }


    public  NewPaky aPakyReceivedByVendor() {
        final NewPaky target = agivenPakySentToVendor();
        receive(target);
        return target;
    }


    public  NewPaky aPakyDestinateToACustomer() {
        final NewPaky target = aPakyReceivedByVendor();
        destinate(target);
        return target;
    }



    public  NewPaky aPakySentToACustomer() {
        final NewPaky target = aPakyDestinateToACustomer();
        deploy(target);
        return target;
    }

    public  void check(Optional<NewPaky> expectedSummary) {
        if (!expectedSummary.isPresent()) fail("paky non inserito");
    }

    public  Optional<NewPaky> givenAPaky() {
        final HttpResponse<NewPaky> expectedPakySummary =
                client.toBlocking().exchange(BASE_PAKY_URL + "/register", NewPaky.class);
        return expectedPakySummary.getBody();
    }

    public  HttpResponse<PakyAnswer> receive(NewPaky target) {
        return postCommand(BASE_PAKY_URL + target.getIdPaky() + "/receive", new ExpeditionCommand("vendor1"));
    }

    public  HttpResponse<PakyAnswer> destinate(NewPaky target) {
        return postCommand(
                BASE_PAKY_URL + target.getIdPaky() + "/destinate", new DestinationCommand("customer1"));
    }

    public  HttpResponse<PakyAnswer> deploy(NewPaky target) {
        return postCommand(BASE_PAKY_URL + target.getIdPaky() + "/deploy", new DestinationCommand("customer1"));
    }

    public  HttpResponse<PakyAnswer> plug(NewPaky target) {
        return postCommand(BASE_PAKY_URL + target.getIdPaky() + "/plug", new DestinationCommand("customer1"));
    }

    public  HttpResponse<PakyAnswer> bookTo(NewPaky target) {
        return postCommand(BASE_PAKY_URL + target.getIdPaky() + "/book", new ExpeditionCommand("vendor1"));
    }

    public  HttpResponse<PakyAnswer> sendTo(NewPaky target) {
        return postCommand(BASE_PAKY_URL + target.getIdPaky() + "/send", new ExpeditionCommand("vendor1"));
    }



    public  HttpResponse<PakyAnswer> postCommand(String uri, Serializable command) {
        return client.toBlocking().exchange(POST(uri, command), PakyAnswer.class);
    }

    public HttpResponse<Paky> search(String idPaky) {
       return client.toBlocking().exchange(BASE_PAKY_URL + idPaky, Paky.class);
    }
}
