package com.masa.integration.paky.endpoint;

import com.masa.endpoint.base.beans.AssociateCommand;
import com.masa.endpoint.base.beans.BaseAnswer;
import com.masa.endpoint.machinery.beans.MachineryAnswer;
import com.masa.endpoint.machinery.beans.MachineryCommand;
import com.masa.paky.paky.entity.Paky;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import liquibase.pro.packaged.B;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BaseConrollerIT extends ControllerBase{
    public static final String BASE = "/api/v1/base/";

    @Test
    void register_createNewBase_response200() {
        final HttpResponse<BaseAnswer> result =
                postCommand(BASE + "register", "{}");
        assertEquals(OK.getCode(), result.getStatus().getCode());
        assertTrue(result.getBody().isPresent());
    }

    @Test
    void register_searchReturnedId_returntheBase() {
        final HttpResponse<BaseAnswer> result =
                postCommand(BASE + "register", "{}");
        assertTrue(result.getBody().isPresent());
        final BaseAnswer base= result.getBody().get();
        final String expectedId = base.getBase().getBaseId();
        final HttpResponse<BaseAnswer> searchResponse =
                client.toBlocking().exchange(BASE + base.getBase().getBaseId(), BaseAnswer.class);
       assertThat(searchResponse)
               .satisfies($ -> assertEquals(OK,$.getStatus()))
               .satisfies($ -> assertTrue($.getBody().isPresent()))
               .satisfies($ -> assertEquals(expectedId,$.getBody().get().getBase().getBaseId()));

    }

    @Test
    void register_afterAssignAndSearch_returntheBaseWithCustomerAssociated() {
        givenExistsCustomer();
        final HttpResponse<BaseAnswer> result =
                postCommand(BASE + "register", "{}");
        assertTrue(result.getBody().isPresent());
        final BaseAnswer base= result.getBody().get();
        final String expectedId = base.getBase().getBaseId();
        postCommand(BASE+base.getBase().getBaseId() + "/assign",new AssociateCommand("customer1"));
        final HttpResponse<BaseAnswer> searchResponse =
                client.toBlocking().exchange(BASE + base.getBase().getBaseId(), BaseAnswer.class);
        assertThat(searchResponse)
                .satisfies($ -> assertEquals("customer1",$.getBody().get().getBase().getCustomerId()));

    }

    private HttpResponse<BaseAnswer> postCommand(String uri, Serializable command) {
        return client.toBlocking().exchange(POST(uri, command), BaseAnswer.class);
    }
}
