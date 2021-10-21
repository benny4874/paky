package com.masa.integration.paky.endpoint;

import com.masa.endpoint.machinery.beans.MachineryAnswer;
import com.masa.endpoint.machinery.beans.MachineryDataAnswer;
import com.masa.endpoint.paky.beans.NewPaky;
import com.masa.paky.machinery.entity.Machinery;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.*;

public class MachineryControllerIT extends ControllerBase {

    @Inject
    PakyActions pakyActions;

    @Inject
    MachineryActions machineryActions;

    public MachineryControllerIT() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_createNewMachinery_response200() {
        final HttpResponse<MachineryAnswer> result = machineryActions.insertNewMachine();
        assertEquals(OK.getCode(), result.getStatus().getCode());
        assertTrue(result.getBody().isPresent());
    }

    @Test
    void find_ExistingMachine_answer200_returnTheMachine() {
        final HttpResponse<MachineryDataAnswer> result =
                machineryActions.insertNewMachine().getBody().map(machineryActions::search).get();

        assertEquals(OK.getCode(), result.getStatus().getCode());
        result.getBody().ifPresentOrElse(machineryActions::check, () -> fail("machinery not in body"));
    }

    @Test
    void find_notExistingMachine_answer400_returnTheMachine() {
        final HttpClientResponseException response =
                assertThrows(HttpClientResponseException.class, () -> machineryActions.search("aMachineThatNOtExists"));
        assertEquals(NOT_FOUND.getCode(), response.getStatus().getCode());
    }

    @Test
    void assign_ExistingVendor_answer200_associateVendor() {
        final Machinery machinery = machineryActions.aGivenMachine();
        final HttpResponse<MachineryDataAnswer> result = machineryActions.assign(machinery, "vendor1");
        assertEquals(OK.getCode(), result.getStatus().getCode());
        machineryActions.search(machinery.getMachineryId())
                .getBody()
                .map(MachineryDataAnswer::getMachinery)
                .ifPresent($ -> assertEquals("vendor1", $.getVendorId()));
    }

    @Test
    void load_validRecipe_answer200_associateRecipe() {
        HttpResponse<MachineryAnswer> result = machineryActions.aGivenMachineLoadRecipe();
        assertEquals(OK, result.getStatus());
        assertTrue(result.getBody().isPresent());
    }


    @Test
    void pakyRefill_return200() {
        final NewPaky paky = pakyActions.registerPaky().body();
        final HttpResponse<MachineryAnswer> result = machineryActions.refillPaky(paky);
        assertEquals(OK, result.getStatus());
    }


}
