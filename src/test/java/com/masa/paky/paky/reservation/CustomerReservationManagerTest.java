package com.masa.paky.paky.reservation;

import com.masa.paky.customer.entity.Customer;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
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

class CustomerReservationManagerTest {
    @Mock
    PakyRepository pakyRepository;
    @Mock
    CustomerRepository customerRepository;
    @Captor
    ArgumentCaptor<Paky> pakyCaptor;

    Paky paky = new Paky();
    Customer customer = new Customer();

    private final CustomerReservationManager underTest;

    public CustomerReservationManagerTest() {
        MockitoAnnotations.openMocks(this);
        paky.setIdPaky("paky");
        customer.setCustomerId("customer");
        underTest = new CustomerReservationManager(customerRepository,pakyRepository);
    }

    @Test
    void reserve_associatePakyToCustomer() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.of(paky));
        when(customerRepository.findById("customer")).thenReturn(Optional.of(customer));
        underTest.reserve("paky", "customer");
        verify(pakyRepository).update(pakyCaptor.capture());
        final Paky result = pakyCaptor.getValue();
        assertEquals(paky, result);
        assertEquals("customer", result.getCustomerId());
    }

    @Test
    void reserve_notExistingPaky_ThrowPakyNotFoundException() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.ofNullable(null));
        when(customerRepository.findById("customer")).thenReturn(Optional.of(customer));
        assertThrows(PakyNotFoundException.class, () -> underTest.reserve("paky", "customer"));
    }

    @Test
    void reserve_notExistingVendor_ThrowCustomerNotFoundException() {
        when(pakyRepository.findById("paky")).thenReturn(Optional.ofNullable(paky));
        when(customerRepository.findById("customer")).thenReturn(Optional.ofNullable(null));
        assertThrows(CustomerNotFoundException.class, () -> underTest.reserve("paky", "customer"));
    }
}
