package com.masa.paky.machinery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import com.masa.paky.machinery.entity.Machinery;
import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.machinery.exception.MachineryNotFoundException;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

class MachienryHandlerTest {

  @Mock VendorRepository vendorRepository;
  @Mock MachineryRepository machineryRepository;

  MachineryHandler underTest;

  public MachienryHandlerTest() {
    openMocks(this);
    underTest = new MachineryHandler(machineryRepository, vendorRepository);
    when(vendorRepository.findById("aVendor")).thenReturn(Optional.of(new Vendor()));
  }

  @Test
  void create_addNewMachinery_withDesiredDescription_notAssociatedToVendor() {
    Machinery result = underTest.create("I am a machinery brand new");
    assertNotNull(result.getMachineryId());
    assertEquals("I am a machinery brand new", result.getDescription());
    assertNull(result.getVendorId());
  }

  @Test
  void create_addNewMachinery_persist() {
    Machinery result = underTest.create("I am a machinery brand new");
    verify(machineryRepository).save(result);
  }

  @Test
  void create_twoTimes_twoDifferentMachinery() {
    Machinery firstMachinery = underTest.create("I am a machinery brand new");
    Machinery secondMachinery = underTest.create("I am a machinery brand new");
    assertNotEquals(firstMachinery, secondMachinery);
  }

  @Test
  void assign_setVendorId() {
    final Machinery aGivenMachine = getAMachine();
    underTest.assign(aGivenMachine.getMachineryId(), "aVendor");
    assertEquals("aVendor", aGivenMachine.getVendorId());
  }

  @Test
  void assign_PersistChanges() {
    final Machinery aGivenMachine = getAMachine();
    underTest.assign(aGivenMachine.getMachineryId(), "aVendor");
    verify(machineryRepository).update(aGivenMachine);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"notExistingVendor"})
  void assign_NotExistingVendor_ThrowVendorNotFoundException(String vendorId) {
    final Machinery aGivenMachine = getAMachine();
    assertThrows(
        VendorNotFoundException.class,
        () -> underTest.assign(aGivenMachine.getMachineryId(), vendorId));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"notExistingVendor"})
  void assign_NotExistingVendor_nothigIssaved(String vendorId) {
    final Machinery aGivenMachine = getAMachine();
    assign(vendorId, aGivenMachine);
    verify(machineryRepository, never()).update(aGivenMachine);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"notExistingMachine"})
  void assign_NotExistingMachine_nothigIssaved(String machineId) {
    try {
      underTest.assign(machineId, "aVendor");
    } catch (Exception e) {
      // do nothing
    }
    verify(machineryRepository, never()).update(any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"notExistingMachine"})
  void assign_NotExistingMachine_ThrowMachineryNotFoundException(String machineId) {
    assertThrows(MachineryNotFoundException.class, () -> underTest.assign(machineId, "aVendor"));
  }

  private void assign(String vendorId, Machinery aGivenMachine) {
    try {
      underTest.assign(aGivenMachine.getMachineryId(), vendorId);
    } catch (VendorNotFoundException expectedException) {
      // do nothing
    }
  }

  private Machinery getAMachine() {
    Machinery machinery = new Machinery();
    machinery.setMachineryId("aMachinery");
    machinery.setDescription("aDescritpion");
    makeSearchable(machinery);
    return machinery;
  }

  private void makeSearchable(Machinery machinery) {
    when(machineryRepository.findById("aMachinery")).thenReturn(Optional.of(machinery));
  }
}
