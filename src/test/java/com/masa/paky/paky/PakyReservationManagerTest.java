package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.vendor.entity.Vendor;
import com.masa.paky.vendor.entity.VendorRepository;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PakyReservationManagerTest {
    @Mock
    PakyRepository pakyRepository;
    @Mock
    VendorRepository vendorRepository;
    @Captor
    ArgumentCaptor<Paky> pakyCaptor;

    Paky paky = new Paky();
    Vendor customer = new Vendor();


    private final PakyReservationManager underTest;

    public PakyReservationManagerTest() {
        MockitoAnnotations.openMocks(this);
        paky.setIdPaky("paky");
        customer.setVendorId("customer");
        underTest = new PakyReservationManager(pakyRepository, vendorRepository);

    }

    @Test
    void reserve_associatePakyToCustomer() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.of(paky));
        when(vendorRepository.findById("customer")).thenReturn(Optional.of(customer));
        underTest.reserve("paky", "customer");
        verify(pakyRepository).update(pakyCaptor.capture());
        final Paky result = pakyCaptor.getValue();
        assertEquals(paky, result);
        assertEquals("customer", result.getVendorId());
    }

    @Test
    void reserve_notExistingPaky_ThrowPakyNotFoundException() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.ofNullable(null));
        when(vendorRepository.findById("customer")).thenReturn(Optional.of(customer));
        assertThrows(PakyNotFoundException.class, () -> underTest.reserve("paky", "customer"));

    }
    @Test
    void reserve_notExistingVendor_ThrowVendorNotFoundException() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.ofNullable(paky));
        when(vendorRepository.findById("customer")).thenReturn(Optional.ofNullable(null));
        assertThrows(VendorNotFoundException.class, () -> underTest.reserve("paky", "customer"));

    }
}