package com.masa.endpoint.machinery;

import static com.masa.paky.recipe.RecipeBuilder.getFor;
import static io.micronaut.http.HttpResponse.*;

import com.masa.endpoint.machinery.beans.MachineryAnswer;
import com.masa.endpoint.machinery.beans.MachineryCommand;
import com.masa.endpoint.machinery.beans.MachineryDataAnswer;
import com.masa.endpoint.machinery.beans.RecipeCommand;
import com.masa.paky.machinery.MachineryHandler;
import com.masa.paky.machinery.entity.Machinery;
import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.machinery.exception.MachineryNotFoundException;
import com.masa.paky.paky.PakyRefillerManager;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.recipe.MachineryRecipeLoader;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.entity.RecipeRepository;
import com.masa.paky.recipe.exceptions.WrongRecipeException;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@ExecuteOn(TaskExecutors.IO)
@Introspected
@Controller("/machinery")
public class MachineryController {
  protected final VendorRepository vendorRepository;
  protected final MachineryRepository machineryRepository;
  protected final RecipeRepository recipeRepository;
  protected final PakyRepository pakyRepository;

  public MachineryController(
      VendorRepository vendorRepository,
      MachineryRepository machineryRepository,
      RecipeRepository recipeRepository,
      PakyRepository pakyRepository) {
    this.vendorRepository = vendorRepository;
    this.machineryRepository = machineryRepository;
    this.recipeRepository = recipeRepository;
    this.pakyRepository = pakyRepository;
  }

  @Post("register")
  public HttpResponse<MachineryAnswer> register(@Body MachineryCommand machineryData) {
    MachineryHandler handler = new MachineryHandler(machineryRepository, vendorRepository);
    final Machinery machinery = handler.create(machineryData.getDescription());
    return ok(new MachineryAnswer(machinery.getMachineryId()));
  }

  @Post("{machineryId}/assign")
  public HttpResponse<MachineryAnswer> assign(
      @PathVariable(value = "machineryId") String machineryId,
      @Body MachineryCommand machineryData) {
    MachineryHandler handler = new MachineryHandler(machineryRepository, vendorRepository);
    try {
      handler.assign(machineryId, machineryData.getVendorId());
      return ok(new MachineryAnswer("Correctly assigned"));
    } catch (VendorNotFoundException noVendor) {
      return notFound(
          new MachineryAnswer("Vendor not found with Id: " + machineryData.getVendorId()));
    }
  }

  @Post("{machineryId}/load")
  public HttpResponse<MachineryAnswer> assign(
      @PathVariable(value = "machineryId") String machineryId, @Body RecipeCommand recipeData) {

    try {
      final Machinery machinery = getMachinery(machineryId);
      bind(machinery, toRecipe(recipeData, machinery.getVendorId()));
      return ok(new MachineryAnswer("Correctly load recipe on machinery"));
    } catch (MachineryNotFoundException noMachine) {
      return notFound(new MachineryAnswer(noMachine.getMessage()));
    } catch (WrongRecipeException errorInRecipe) {
      return badRequest(new MachineryAnswer(errorInRecipe.getMessage()));
    }
  }

  private void bind(Machinery machinery, Recipe recipe) {
    final MachineryRecipeLoader machineryRecipeLoader =
        new MachineryRecipeLoader(recipe, machinery, recipeRepository, machineryRepository);
    machineryRecipeLoader.load();
  }

  private Recipe toRecipe(RecipeCommand recipeData, String vendorId) {
    return getFor(vendorId)
        .with(
            $ -> {
              $.unit = recipeData.getUnit();
              $.quantity = recipeData.getQuantity();
              $.label = recipeData.getLabel();
              $.expiration = recipeData.getExpiration();
              $.description = recipeData.getDescription();
              $.brand = recipeData.getBrand();
            })
        .build();
  }

  private Machinery getMachinery(String machineryId) {
    return machineryRepository
        .findById(machineryId)
        .orElseThrow(() -> new MachineryNotFoundException(machineryId));
  }

  @Get("{machineryId}")
  public HttpResponse<MachineryDataAnswer> findById(
      @PathVariable(value = "machineryId") String machineryId) {
    return machineryRepository
        .findById(machineryId)
        .map(this::wrapInOkResponse)
        .orElseGet(() -> answerNotFound(machineryId));
  }

  @Post("{machineryId}/{pakyId}")
  public HttpResponse<MachineryAnswer> refill(
      @PathVariable(value = "machineryId") String machineryId,
      @PathVariable(value = "pakyId") String pakyId) {
    try {
      PakyRefillerManager refiller = new PakyRefillerManager(pakyRepository, machineryRepository);
      refiller.refill(machineryId, pakyId);
      return ok(new MachineryAnswer("Paky " + pakyId + " refilled successefully"));
    } catch (PakyNotFoundException | MachineryNotFoundException error) {
      return notFound(new MachineryAnswer(error.getMessage()));
    }
  }

  private HttpResponse<MachineryDataAnswer> wrapInOkResponse(Machinery $) {
    return ok(new MachineryDataAnswer("OK", $));
  }

  private MutableHttpResponse<MachineryDataAnswer> answerNotFound(String machineryId) {
    return notFound(new MachineryDataAnswer("Machinery not found with id: " + machineryId, null));
  }
}
