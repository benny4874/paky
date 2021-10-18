package com.masa.paky.base;

import com.masa.paky.base.entity.Base;
import com.masa.paky.base.entity.BaseRepository;
import com.masa.paky.base.exceptions.BaseNotFoundException;
import com.masa.paky.customer.entity.Customer;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class BaseOperationHandlerTest {
    @Mock
    CustomerRepository customerRepository;
    @Mock
    BaseRepository repository;

    @InjectMocks
    BaseOperationHandler underTest;

    @Captor
    ArgumentCaptor<Base> captor;

    public BaseOperationHandlerTest() {
        openMocks(this);
    }

    @Test
    void register_saveNewBaseWithOnlyId() {
        final Base result = underTest.register();
        verify(repository).save(captor.capture());
        final Base savedEntity = captor.getValue();
        assertNotNull(savedEntity.getBaseId());
        assertSame(result, savedEntity);
    }

    @Test
    void associate_bindToCustomer() {
        final Base aBase = aGivenBase();
        letCustomer1Exists();
        underTest.associate(aBase.getBaseId(), "customer1");
        verify(repository).update(captor.capture());
        assertEquals("customer1",captor.getValue().getCustomerId());
    }

    private void letCustomer1Exists() {
        when(customerRepository.findById("customer1")).thenReturn(of(getACustomerWithId("customer1")));
    }

    @Test
    void associate_notExistingBase_throwsBaseNotFoundException(){
        letCustomer1Exists();
        assertThatExceptionOfType(BaseNotFoundException.class)
                .isThrownBy(() ->
                        underTest.associate("baseThatNotExists","customer1"));
    }


    @Test
    void associate_notExistingCustomer_throwsCustomerNotFoundException(){
        final Base aBase = aGivenBase();
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() ->
                        underTest.associate(aBase.getBaseId(),"customerThatNotExists"));
    }

    private Customer getACustomerWithId(String customerId) {
        Customer cusotmer = new Customer();
        cusotmer.setCustomerId(customerId);
        return cusotmer;
    }

    private Base aGivenBase() {
        final Base base = underTest.register();
        when(repository.findById(base.getBaseId())).thenReturn(of(base));
        return base;
    }

}
