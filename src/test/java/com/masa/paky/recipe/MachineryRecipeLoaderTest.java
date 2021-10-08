package com.masa.paky.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import com.masa.paky.machinery.entity.Machinery;
import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.machinery.exception.MachineryNotFoundException;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.entity.RecipeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

class MachineryRecipeLoaderTest {

  @Mock RecipeRepository recipeRepository;
  @Mock MachineryRepository machineryRepository;
  @Captor ArgumentCaptor<Machinery> machineryCaptor;

  Machinery machinery = new Machinery();
  Recipe recipe = new Recipe();

  public MachineryRecipeLoaderTest() {
    openMocks(this);
    machinery.setMachineryId("aMachine");
    recipe.setRecipeId("aRecipe");
    when(machineryRepository.findById("aMachine")).thenReturn(Optional.of(machinery));
  }

  @Test
  void load_saveRecipe() {
    MachineryRecipeLoader underTest =
        new MachineryRecipeLoader(recipe, machinery, recipeRepository, machineryRepository);
    underTest.load();
    verify(recipeRepository).save(recipe);
  }

  @Test
  void load_notExistingMachine_throesMachineryNotFoundException() {
    MachineryRecipeLoader underTest =
        new MachineryRecipeLoader(
            recipe, aGivenMachineThatNotExists(), recipeRepository, machineryRepository);
    assertThrows(MachineryNotFoundException.class, underTest::load);
  }

  @Test
  void load_afterSavingReceipeBindToMachinery() {
    MachineryRecipeLoader underTest =
        new MachineryRecipeLoader(recipe, machinery, recipeRepository, machineryRepository);
    underTest.load();
    InOrder inOrder = inOrder(recipeRepository, machineryRepository);
    inOrder.verify(recipeRepository).save(recipe);
    inOrder.verify(machineryRepository).update(machineryCaptor.capture());
    final Machinery result = machineryCaptor.getValue();
    assertAll(
        () -> assertEquals(recipe.getRecipeId(), result.getRecipeId()),
        () -> assertEquals(machinery, result));
  }

  private Machinery aGivenMachineThatNotExists() {
    Machinery notExistentMachinery = new Machinery();
    notExistentMachinery.setMachineryId("aMachineThatNotExists");
    return notExistentMachinery;
  }
}
