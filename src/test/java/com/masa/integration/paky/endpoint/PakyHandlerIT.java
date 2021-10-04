package com.masa.integration.paky.endpoint;

import com.masa.endpoint.paky.beans.ExpeditionCommand;
import com.masa.endpoint.paky.beans.NewPaky;
import com.masa.endpoint.paky.beans.PakyAnswer;
import com.masa.paky.paky.entity.Paky;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpStatus.OK;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



class PakyHandlerIT extends ControllerBase {

    public static final String BASE = "/paky/";


    public PakyHandlerIT() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_createNewPakyWithEndPoint_response200() {
        GET("/paky/register");
        final HttpResponse<NewPaky> result = client.toBlocking().exchange("/paky/register", NewPaky.class);
        assertEquals(OK.getCode(), result.getStatus().getCode());
        assertTrue(result.getBody().isPresent());
    }


    @Test
    void registerPaky_searchReturnedId_returnthePaky() {
        final Optional<NewPaky> expectedSummary = givenAPaky();
        final HttpResponse<Paky> searchResponse = client.toBlocking().exchange("/paky/" + expectedSummary.get().getIdPaky(), Paky.class);
        assertEquals(OK.getCode(), searchResponse.getStatus().getCode());
        assertTrue(searchResponse.getBody().isPresent());
        final Paky result = searchResponse.getBody().get();
        assertEquals(expectedSummary.get().getIdPaky(), result.getIdPaky());
        assertEquals(expectedSummary.get().getTopic(), result.getTopic());
    }

    @Test
    void registerPaky_bookPakyForCustomer_wellDone() {
        givenExistsVendor();
        final Optional<NewPaky> expectedSummary = givenAPaky();
        check(expectedSummary);
        final NewPaky target = expectedSummary.get();
        final HttpResponse<PakyAnswer> result = bookTo(target);
        assertEquals(OK.getCode(), result.status().getCode());
    }

    @Test
    void sendPaky_bookPakyForCustomer_wellDone() {
        givenExistsVendor();
        final Optional<NewPaky> expectedSummary = givenAPaky();
        check(expectedSummary);
        final NewPaky target = expectedSummary.get();
        bookTo(target);
        final HttpResponse<PakyAnswer> result = sendTo(target);
        assertEquals(OK.getCode(), result.status().getCode());
    }




    @Test
    void receivePaky_bookPakyForCustomer_wellDone() {
        givenExistsVendor();
        final Optional<NewPaky> expectedSummary = givenAPaky();
        check(expectedSummary);
        final NewPaky target = expectedSummary.get();
        bookTo(target);
        sendTo(target);
        final HttpResponse<PakyAnswer> result = receive(target);
        assertEquals(OK.getCode(), result.status().getCode());
    }



    private void check(Optional<NewPaky> expectedSummary) {
        if (!expectedSummary.isPresent()) fail("paky non inserito");
    }

    private Optional<NewPaky> givenAPaky() {
        final HttpResponse<NewPaky> expectedPakySummary = client.toBlocking().exchange("/paky/register", NewPaky.class);
        return expectedPakySummary.getBody();
    }

    private HttpResponse<PakyAnswer> receive(NewPaky target) {
        return postCommand(BASE + target.getIdPaky() + "/receive", new ExpeditionCommand("vendor1"));
    }

    private HttpResponse<PakyAnswer> bookTo(NewPaky target) {
        return postCommand(BASE + target.getIdPaky() + "/book", new ExpeditionCommand("vendor1"));
    }

    private HttpResponse<PakyAnswer> sendTo(NewPaky target) {
        return postCommand(BASE + target.getIdPaky() + "/send", new ExpeditionCommand("vendor1"));
    }

    private HttpResponse<PakyAnswer> postCommand(String uri, ExpeditionCommand command) {
        return client.toBlocking().exchange(POST(uri, command
        ), PakyAnswer.class);
    }
}
