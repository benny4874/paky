package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;


public class PakyReporter {
    private final PakyLifeCycleHandlerFactory factory;
    private final PakyRepository repository;

    public PakyReporter(PakyRepository repository) {
        this.repository = repository;
        this.factory = new PakyLifeCycleHandlerFactory(repository);
    }

    public void report(String pakyId, float quantity){
        factory
                  .getFor(getPaky(pakyId))
                  .report(quantity);
    }

    private Paky getPaky(String pakyId) {
       return repository
                .findById(pakyId)
                .orElseThrow(() -> new PakyNotFoundException(pakyId));
    }
}
