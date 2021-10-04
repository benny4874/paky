package com.masa.integration.paky.endpoint;

import com.masa.endpoint.machinery.beans.MachineryAnswer;
import com.masa.endpoint.machinery.beans.MachineryCommand;
import com.masa.endpoint.machinery.beans.MachineryDataAnswer;
import com.masa.paky.machinery.entity.Machinery;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.*;

public class MachineryControllerIT extends ControllerBase{

    public static final String BASE = "/machinery/";

    public MachineryControllerIT() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_createNewMachinery_response200() {
        final HttpResponse<MachineryAnswer> result = insertNewMachine();
        assertEquals(OK.getCode(), result.getStatus().getCode());
        assertTrue(result.getBody().isPresent());
    }


    @Test
    void find_ExistingMachine_answer200_returnTheMachine() {
        final HttpResponse<MachineryDataAnswer> result =
                insertNewMachine()
                .getBody()
                .map(this::search)
                .get();

        assertEquals(OK.getCode(), result.getStatus().getCode());
        result.getBody().ifPresentOrElse(
                this::check,
                () -> fail("machinery not in body")
        );
    }

    @Test
    void find_notExistingMachine_answer400_returnTheMachine() {
        final HttpClientResponseException response = assertThrows(HttpClientResponseException.class,
                () ->
                        search("aMachineThatNOtExists")
        );
      assertEquals(NOT_FOUND.getCode(), response.getStatus().getCode());
    }
    @Test
    void assign_ExistingVendor_answer200_associateVendor() {
        givenExistsVendor();
        final Machinery machinery = aGivenMachine();
        final HttpResponse<MachineryDataAnswer> result = assign(machinery, "vendor1");

        assertEquals(OK.getCode(), result.getStatus().getCode());
        search(machinery.getMachineryId())
                .getBody()
                .map(MachineryDataAnswer::getMachinery)
                .ifPresent($ -> assertEquals("vendor1",$.getVendorId()));

    }

    private Machinery aGivenMachine() {
        return insertNewMachine()
                .getBody()
                .map(this::search)
                .flatMap(HttpResponse::getBody)
                .get()
                .getMachinery();

    }

    private HttpResponse<MachineryDataAnswer> assign(Machinery machinery, String vendorId) {
        final MachineryCommand machineryCommand = new MachineryCommand();
        machineryCommand.setVendorId(vendorId);
        return postAssignCommand(BASE + machinery.getMachineryId() + "/assign", machineryCommand);
    }


    private void check(MachineryDataAnswer machineryDataAnswer) {
        final Machinery machinery = machineryDataAnswer.getMachinery();
        assertEquals("description",machinery.getDescription());
        assertNull(machinery.getVendorId());
    }

    private HttpResponse<MachineryDataAnswer> search(MachineryAnswer machineryAnswer) {
        final String machineryId = machineryAnswer.getReturnMessage();
        return search(machineryId);
    }

    private HttpResponse<MachineryDataAnswer> search(String machineryId) {
        return client.toBlocking().exchange(BASE + machineryId, MachineryDataAnswer.class);
    }


    private HttpResponse<MachineryAnswer> insertNewMachine() {
        final MachineryCommand machineryCommand = new MachineryCommand();
        machineryCommand.setDescription("description");
        return postCommand(BASE + "register", machineryCommand);
    }

    private HttpResponse<MachineryAnswer> postCommand(String uri, MachineryCommand command) {
        return client.toBlocking().exchange(POST(uri, command
        ), MachineryAnswer.class);
    }

    private HttpResponse<MachineryDataAnswer> postAssignCommand(String uri, MachineryCommand command) {
        return client.toBlocking().exchange(POST(uri, command
        ), MachineryDataAnswer.class);
    }
}


