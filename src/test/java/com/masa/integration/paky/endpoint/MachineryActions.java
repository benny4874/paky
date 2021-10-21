package com.masa.integration.paky.endpoint;

import com.masa.endpoint.machinery.beans.MachineryAnswer;
import com.masa.endpoint.machinery.beans.MachineryCommand;
import com.masa.endpoint.machinery.beans.MachineryDataAnswer;
import com.masa.endpoint.machinery.beans.RecipeCommand;
import com.masa.endpoint.paky.beans.NewPaky;
import com.masa.paky.machinery.entity.Machinery;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Date;

import static com.masa.integration.paky.endpoint.ControllerBase.BASE_MACHINERY_URL;
import static io.micronaut.http.HttpRequest.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Singleton
class MachineryActions {

    @Inject
    @Client("/")
    HttpClient client;
    
    public  HttpResponse<MachineryAnswer> refillPaky(NewPaky paky) {
        final Machinery machinery = aGivenMachine();
        loadRecipe(machinery);
        return postEmptyBodyCommand(BASE_MACHINERY_URL + machinery.getMachineryId() + "/" + paky.getIdPaky());
    }

    public  HttpResponse<MachineryAnswer> aGivenMachineLoadRecipe() {
        final Machinery machinery = aGivenMachine();
        return loadRecipe(machinery);
    }

    public HttpResponse<MachineryAnswer> loadRecipe(Machinery machinery) {
        assign(machinery, "vendor1");
        RecipeCommand recipe = getRecipeCommand();
        return postRecipeCommand(BASE_MACHINERY_URL + machinery.getMachineryId() + "/load", recipe);
    }

    public  RecipeCommand getRecipeCommand() {
        RecipeCommand recipe = new RecipeCommand();
        recipe.setBrand("brand");
        recipe.setDescription("bucatini");
        recipe.setLabel("[{ \"label\": \"value\" }]");
        recipe.setQuantity(100);
        recipe.setUnit("gr");
        recipe.setExpiration(Date.from(Instant.now().plusSeconds(1000)));
        return recipe;
    }

    public  Machinery aGivenMachine() {
        return insertNewMachine()
                .getBody()
                .map(this::search)
                .flatMap(HttpResponse::getBody)
                .get()
                .getMachinery();
    }

    public  HttpResponse<MachineryDataAnswer> assign(Machinery machinery, String vendorId) {
        final MachineryCommand machineryCommand = new MachineryCommand();
        machineryCommand.setVendorId(vendorId);
        return postAssignCommand(BASE_MACHINERY_URL + machinery.getMachineryId() + "/assign", machineryCommand);
    }

    public  void check(MachineryDataAnswer machineryDataAnswer) {
        final Machinery machinery = machineryDataAnswer.getMachinery();
        assertEquals("description", machinery.getDescription());
        assertNull(machinery.getVendorId());
    }

    public  HttpResponse<MachineryDataAnswer> search(MachineryAnswer machineryAnswer) {
        final String machineryId = machineryAnswer.getReturnMessage();
        return search(machineryId);
    }

    public  HttpResponse<MachineryDataAnswer> search(String machineryId) {
        return client.toBlocking().exchange(BASE_MACHINERY_URL + machineryId, MachineryDataAnswer.class);
    }

    public  HttpResponse<MachineryAnswer> insertNewMachine() {
        final MachineryCommand machineryCommand = new MachineryCommand();
        machineryCommand.setDescription("description");
        return postCommand(BASE_MACHINERY_URL + "register", machineryCommand);
    }

    public  HttpResponse<MachineryAnswer> postCommand(String uri, MachineryCommand command) {
        return client.toBlocking().exchange(POST(uri, command), MachineryAnswer.class);
    }

    public  HttpResponse<MachineryAnswer> postEmptyBodyCommand(String uri) {
        return client.toBlocking().exchange(POST(uri, "{}"), MachineryAnswer.class);
    }

    public  HttpResponse<MachineryAnswer> postRecipeCommand(String uri, RecipeCommand command) {
        return client.toBlocking().exchange(POST(uri, command), MachineryAnswer.class);
    }

    public  HttpResponse<MachineryDataAnswer> postAssignCommand(
            String uri, MachineryCommand command) {
        return client.toBlocking().exchange(POST(uri, command), MachineryDataAnswer.class);
    }
}
