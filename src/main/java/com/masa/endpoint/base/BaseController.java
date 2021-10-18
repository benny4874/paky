package com.masa.endpoint.base;


import com.masa.endpoint.base.beans.AssociateCommand;
import com.masa.endpoint.base.beans.BaseAnswer;
import com.masa.paky.base.BaseOperationHandler;
import com.masa.paky.base.entity.Base;
import com.masa.paky.base.entity.BaseRepository;
import com.masa.paky.base.exceptions.BaseNotFoundException;
import com.masa.paky.customer.entity.CustomerRepository;
import com.masa.paky.customer.exceptions.CustomerNotFoundException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.Optional;

import static io.micronaut.http.HttpResponse.notFound;
import static io.micronaut.http.HttpResponse.ok;

@ExecuteOn(TaskExecutors.IO)
@Introspected
@Controller("/api/v1/base")
public class BaseController {

    protected final CustomerRepository customerRepository;
    protected final BaseRepository baseRepository;
    protected final BaseOperationHandler operationHandler;

    public BaseController(CustomerRepository customerRepository, BaseRepository baseRepository) {
        this.customerRepository = customerRepository;
        this.baseRepository = baseRepository;
        operationHandler = new BaseOperationHandler(baseRepository,customerRepository);
    }

    @Post("/register")
    public HttpResponse<BaseAnswer> register(){
        final Base newItem = operationHandler.register();
        return ok(new BaseAnswer(newItem));
    }

    @Post("{baseId}/assign")
    public HttpResponse<BaseAnswer> associate(@PathVariable(value = "baseId") String baseId,
                                              @Body AssociateCommand command){
       try {
           operationHandler.associate(baseId, command.getCustomerId());
       } catch(BaseNotFoundException | CustomerNotFoundException error){
           return notFound(new BaseAnswer(error.getMessage()));
       }
       return ok(new BaseAnswer("base: " + baseId + " is now associated to: " + command.getCustomerId()));
    }

    @Get("{baseId}")
    public HttpResponse<BaseAnswer> findById(@PathVariable(value = "baseId") String baseId){
        final Optional<Base> base = baseRepository
                .findById(baseId);
        if (base.isPresent())
            return ok(new BaseAnswer(base.get()));
        else
            return notFound(new BaseAnswer("Base with id: " + baseId + " does not exists"));
    }
}
