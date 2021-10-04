package com.masa.paky.paky;

import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.recipe.RecipeBuilder;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.exceptions.RecipeNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.function.Consumer;

import static com.masa.paky.paky.PakyLifeCycleHandlerTest.A_DATE_IN_TIME;
import static com.masa.paky.paky.entity.PakyStatus.FILLED;
import static com.masa.paky.recipe.RecipeBuilder.getFor;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PakyRefillerManagerTest {

    @Mock
    PakyRepository pakyRepository;

    @Mock
    MachineryRepository machineryRepository;

    Paky paky;

    private final Consumer<RecipeBuilder> aBaunchOfData = $ -> {
        $.brand = "brand";
        $.description = "description";
        $.expiration = A_DATE_IN_TIME;
        $.label = "{\"aString\": \"anotherString\"}";
        $.quantity = 100;
        $.unit = "gr";
    };




    PakyRefillerManager underTest;

    public PakyRefillerManagerTest() {
        openMocks(this);
        underTest = new PakyRefillerManager(pakyRepository,machineryRepository);
        Recipe recipe = getARecipe();
        letPakyReadyToBeFilled();
        when(pakyRepository.findById("aPaky")).thenReturn(of(paky));
        when(machineryRepository.findActualRecipeFor("aMachine")).thenReturn(of(recipe));
    }

    private void letPakyReadyToBeFilled() {
        paky = new Paky();
        paky.setIdPaky("aPaky");
        paky.setStep(PakyStatus.DELIVERED);

    }

    private Recipe getARecipe() {
        return getFor("avendor")
                .with(aBaunchOfData)
                .build();
    }

    @Test
    void fill_wellDoneDay_fillPaky(){
        underTest.refill("aMachine","aPaky");
        assertAll(
                () -> assertEquals(1, paky.getQuantitaPct()),
                () -> assertEquals(100, paky.getQuantita()),
                () -> assertEquals("description", paky.getProductTypeId()),
                () -> assertNotNull(paky.getPackingDate()),
                () -> assertEquals(FILLED, paky.getStep()),
                () -> assertEquals("brand", paky.getBrand()),
                () -> assertEquals("{\"aString\": \"anotherString\"}", paky.getLabel()),
                () -> assertEquals(A_DATE_IN_TIME, paky.getExpiration()),
                () -> assertEquals("gr", paky.getUnit())
        );
    }

    @Test
    void fill_pakyNotExist_fillPaky(){
        assertThrows(PakyNotFoundException.class, ()-> underTest.refill("aMachine","aPakyThatNotExists"));
    }

    @Test
    void fill_recipeOrMachineryNotExist_fillPaky(){
        assertThrows(RecipeNotFoundException.class, ()-> underTest.refill("somethingNotInDatabase","aPaky"));
    }

}
