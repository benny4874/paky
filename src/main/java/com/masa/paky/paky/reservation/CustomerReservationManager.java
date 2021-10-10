package com.masa.paky.paky.reservation;

import com.masa.paky.customer.entity.Customer;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import com.masa.paky.exceptions.SubjectNotFoundException;
import com.masa.paky.paky.PakyLifeCycleHandler;
import com.masa.paky.paky.entity.PakyRepository;

public class CustomerReservationManager extends ReservationManager<Customer, String, CustomerRepository> {

    public CustomerReservationManager(CustomerRepository subjectRepository, PakyRepository pakyRepository) {
        super(subjectRepository, pakyRepository);
    }

    @Override
    protected SubjectNotFoundException raiseSubjectNotFound(String subjectId) {
        return new CustomerNotFoundException(subjectId);
    }

    @Override
    protected void assign(String subjectId, PakyLifeCycleHandler lifeCycleHandler) {
        lifeCycleHandler.book(subjectId);
    }
}
