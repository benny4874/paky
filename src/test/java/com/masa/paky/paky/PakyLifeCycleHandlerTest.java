package com.masa.paky.paky;

import static com.masa.paky.paky.entity.PakyStatus.*;
import static java.util.Date.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.masa.paky.paky.entity.*;
import com.masa.paky.recipe.RecipeBuilder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PakyLifeCycleHandlerTest {
  private static final Instant INSTANT_IN_TIME =
      LocalDate.now(ZoneId.systemDefault()).plusMonths(10).atStartOfDay().toInstant(ZoneOffset.UTC);
  public static final Date A_DATE_IN_TIME = from(INSTANT_IN_TIME);

  @Mock PakyRepository pakyRepository;

  PakyLifeCycleHandlerFactory lifeCycleHandlerFactory;

  public PakyLifeCycleHandlerTest() {
    MockitoAnnotations.openMocks(this);
    lifeCycleHandlerFactory = new PakyLifeCycleHandlerFactory(pakyRepository);
  }

  private Paky createNewPaky() {
    return getTestPaky(lifeCycleHandlerFactory.createNew());
  }

  private void verifyIsSaved(Paky paky) {
    verify(pakyRepository).save(paky);
  }

  private void verifyIsUpdated(Paky paky) {
    verify(pakyRepository).save(paky);
  }

  @Test
  void twoNewPAky_twoId() {
    final Paky first = createNewPaky();
    final Paky second = createNewPaky();
    assertNotEquals(first.getIdPaky(), second.getIdPaky());
  }

  @Test
  void newPaky_isEmpty() {
    final Paky paky = createNewPaky();
    assertEquals(paky.getQuantity(), 0f);
    assertEquals(paky.getQuantityPct(), 0f);
  }

  @Test
  void newPaky_TraciabilityStatuOk_andNoError() {
    final Paky paky = createNewPaky();
    assertThat(paky)
        .hasFieldOrPropertyWithValue("traciabilityStatus", TraciabilityStatus.OK)
        .hasFieldOrPropertyWithValue("errorCode", ErrorStatus.OK);
  }

  @Test
  void newPaky_isSaved() {
    final Paky paky = createNewPaky();
    verifyIsSaved(paky);
  }

  @Test
  void newPaky_hasTopicAssigned() {
    final Paky paky = createNewPaky();
    assertEquals(paky.getIdPaky() + "/communication", paky.getTopic());
  }

  @Test
  void newPaky_isCreatedNow_modifiedNow() {
    final Paky paky = createNewPaky();
    assertEquals(paky.getDateCreated(), paky.getLastAction());
  }

  @Test
  void newPaky_statusCreated() {
    final Paky paky = createNewPaky();
    assertEquals(paky.getStep(), CREATED);
  }

  @Test
  void assignedPaky_hasStatusAssigned_supplierIdSet() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.bookFor("XXX");
    assertEquals(aPaky.getVendorId(), "XXX");
    assertEquals(ASSIGNED, aPaky.getStep());
    verifyIsUpdated(aPaky);
  }

  @Test
  void deliveredPaky_hasStatusAssigned() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.bookFor("XXX");
    underTest.deliverToReseller();
    assertEquals(DELIVERED, aPaky.getStep());
    verifyIsUpdated(aPaky);
  }

  @Test
  void filledPaky_hasQuantitySet_pct100_productIdSet_statusFILLED() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.fill(
        aRecipe()
            .with(
                $ -> {
                  $.quantity = 100;
                  $.description = "productId";
                  $.brand = "brand";
                  $.label = "{ \"a label\" : \"a value \" }";
                  $.expiration = A_DATE_IN_TIME;
                  $.unit = "unit";
                })
            .build());
    assertAll(
        () -> assertEquals(1, aPaky.getQuantityPct()),
        () -> assertEquals(100, aPaky.getQuantity()),
        () -> assertEquals("productId", aPaky.getProductTypeId()),
        () -> assertNotNull(aPaky.getPackingDate()),
        () -> assertEquals(FILLED, aPaky.getStep()),
        () -> assertEquals("brand", aPaky.getBrand()),
        () -> assertEquals("{ \"a label\" : \"a value \" }", aPaky.getLabel()),
        () -> assertEquals(A_DATE_IN_TIME, aPaky.getExpiration()),
        () -> assertEquals("unit", aPaky.getUnit()));

    verifyIsUpdated(aPaky);
  }

  private RecipeBuilder aRecipe() {
    return RecipeBuilder.getFor("aVendor");
  }

  @Test
  void pakyBookedByCustomer_hasStatusSOLD_resellerIdSet() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.book("customerId");
    assertEquals(aPaky.getStep(), SOLD);
    assertEquals(aPaky.getCustomerId(), "customerId");
    verifyIsUpdated(aPaky);
  }

  @Test
  void pakySentToCustomer_hasStatusTransit() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.book("customerId");
    underTest.sendToCustomer();
    assertEquals(INTRANSIT, aPaky.getStep());
    verifyIsUpdated(aPaky);
  }

  @Test
  void pakyReceivedByCustomer_hasStatusOPERATING() {
    PakyLifeCycleHandler underTest = givenAPakyDeliveredToCustomer();
    assertEquals(OPERATING, getActualStep(underTest));
    verifyIsUpdated(getTestPaky(underTest));
  }

  private PakyLifeCycleHandler givenAPakyDeliveredToCustomer() {
    final Paky aPaky = createNewPaky();
    PakyLifeCycleHandler underTest = lifeCycleHandlerFactory.getFor(aPaky);
    underTest.book("customerId");
    underTest.sendToCustomer();
    underTest.plug();
    return underTest;
  }

  @Test
  void paky_reportStatus_UpdateQuantity_statusNotChange() {
    PakyLifeCycleHandler underTest = givenAPakyDeliveredToCustomer();
    final PakyStatus expectedStatus = getActualStep(underTest);
    underTest.report(10);
    assertEquals(10, getTestPaky(underTest).getQuantity());
    assertEquals(expectedStatus, getActualStep(underTest));
    verifyIsUpdated(getTestPaky(underTest));
  }

  private Paky getTestPaky(PakyLifeCycleHandler underTest) {
    return underTest.get();
  }

  private PakyStatus getActualStep(PakyLifeCycleHandler underTest) {
    return getTestPaky(underTest).getStep();
  }
}
