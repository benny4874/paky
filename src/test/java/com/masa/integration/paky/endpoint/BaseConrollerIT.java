package com.masa.integration.paky.endpoint;

import com.masa.endpoint.base.beans.BaseAnswer;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.micronaut.http.HttpStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseConrollerIT extends ControllerBase{
    @Inject
    BaseActions baseActions;

    @Test
    void register_createNewBase_response200() {
        final HttpResponse<BaseAnswer> result =
                baseActions.register();
        assertEquals(OK.getCode(), result.getStatus().getCode());
        assertTrue(result.getBody().isPresent());
    }

    @Test
    void register_searchReturnedId_returntheBase() {
        final HttpResponse<BaseAnswer> result =
                baseActions.register();
        assertTrue(result.getBody().isPresent());
        final BaseAnswer base= result.getBody().get();
        final String expectedId = base.getBase().getBaseId();
        final HttpResponse<BaseAnswer> searchResponse =
                baseActions.search(base);
       assertThat(searchResponse)
               .satisfies($ -> assertEquals(OK,$.getStatus()))
               .satisfies($ -> assertTrue($.getBody().isPresent()))
               .satisfies($ -> assertEquals(expectedId,$.getBody().get().getBase().getBaseId()));

    }



    @Test
    void register_afterAssignAndSearch_returntheBaseWithCustomerAssociated() {
        final HttpResponse<BaseAnswer> result =
                baseActions.register();
        assertTrue(result.getBody().isPresent());
        final BaseAnswer base= result.getBody().get();
        baseActions.assign(base);
        final HttpResponse<BaseAnswer> searchResponse =
                baseActions.search(base);
        assertThat(searchResponse)
                .satisfies($ -> assertEquals("customer1",$.getBody().get().getBase().getCustomerId()));
    }



}
