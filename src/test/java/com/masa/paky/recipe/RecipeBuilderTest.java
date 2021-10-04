package com.masa.paky.recipe;

import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.exceptions.InvalidLabelException;
import com.masa.paky.recipe.exceptions.InvalidQuantityException;
import com.masa.paky.recipe.exceptions.NoExpirationDateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Date.from;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

class RecipeBuilderTest {

    private static final Instant INSTANT_IN_THE_PAST = LocalDate.of(2000, 01, 01)
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant();
    public static final Date A_DATE_IN_PAST = from(INSTANT_IN_THE_PAST);

    private final Consumer<RecipeBuilder> aBaunchOfData = $ -> {
        $.brand = "brand";
        $.description = "description";
        $.expiration = new Date();
        $.label = "{\"aString\": \"anotherString\"}";
        $.quantity = 100;
        $.unit = "gr";
    };

    public static Stream<Arguments> getInvalidRecipe() {
        Consumer<RecipeBuilder> noExpiration = $ -> $.expiration = null;
        Consumer<RecipeBuilder> pastExpirationDate = $ -> $.expiration = A_DATE_IN_PAST;
        Consumer<RecipeBuilder> labelNotValidJson = $ -> $.label = "xxx";
        Consumer<RecipeBuilder>  labelNull = $ -> $.label = null;
        Consumer<RecipeBuilder> negativeQuantity = $ -> $.quantity = -1;
        Consumer<RecipeBuilder> zeroQuantity = $ -> $.quantity = 0;

        return Stream.of(
          Arguments.of( noExpiration,NoExpirationDateException.class, "Expiration date is null"),
          Arguments.of( pastExpirationDate,NoExpirationDateException.class, "Expiration date is invalid"),
          Arguments.of( labelNotValidJson, InvalidLabelException.class, "Label is in invalid json format"),
          Arguments.of( labelNull,InvalidLabelException.class, "Label is  null"),
          Arguments.of( negativeQuantity,InvalidQuantityException.class, "Quantity is invalid (must not be negative)"),
          Arguments.of( zeroQuantity, InvalidQuantityException.class, "Quantity is invalid (must be greater than 0)")
        );
    }

    @Test
    void build_assignId() {
        final Recipe result = RecipeBuilder.getFor("aVendor")
                .with(
                        aBaunchOfData
                )
                .build();
        assertNotNull(result.getRecipeId());
    }

    @Test
    void with_recipeIsCorrectlyCopied() {
        final Recipe result = RecipeBuilder.getFor("aVendor")
                .with(
                        aBaunchOfData
                )
                .build();
        assertAll(
                () -> assertEquals("brand", result.getBrand()),
                () -> assertEquals("description", result.getDescription()),
                () -> assertNotNull(result.getExpiration()),
                () -> assertEquals("{\"aString\": \"anotherString\"}", result.getLabel()),
                () -> assertEquals(100, result.getQuantity()),
                () -> assertEquals("gr", result.getUnit())
        );
    }

    @ParameterizedTest(name = "{2} should throws {1}")
    @MethodSource("getInvalidRecipe")
    void build_throwsExceptionOnInvalidData(Consumer<RecipeBuilder> wrongData, Class<RuntimeException> exception, String errorDescription) {
        assertThatExceptionOfType(exception)
                .isThrownBy(
                        () -> aGivenReceipt()
                                .with(wrongData)
                                .build())
                .withMessage(errorDescription);
    }

    private RecipeBuilder aGivenReceipt() {
        return RecipeBuilder.getFor("aVendor")
                .with(
                        aBaunchOfData
                );
    }
}
