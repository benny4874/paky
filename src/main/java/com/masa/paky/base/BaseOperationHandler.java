package com.masa.paky.base;

import com.masa.paky.base.entity.Base;
import com.masa.paky.base.entity.BaseRepository;
import com.masa.paky.base.exceptions.BaseNotFoundException;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;


import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
public class BaseOperationHandler {
    private final BaseRepository repository;
    private final CustomerRepository customerRepository;

    public Base register() {
        Base base = new Base();
        base.setBaseId(randomUUID().toString());
        repository.save(base);
        return base;
    }

    public void associate(String baseId,String customerId){
        final Base base = getBase(baseId);
        base.setCustomerId(getCustomerId(customerId));
        repository.update(base);
    }

    private String getCustomerId(String customerId) {
        return customerRepository
                .findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId))
                .getCustomerId();
    }

    private Base getBase(String baseId) {
        return repository.findById(baseId)
                .orElseThrow(() -> new BaseNotFoundException(baseId));
    }
}
