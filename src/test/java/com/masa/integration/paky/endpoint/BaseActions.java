package com.masa.integration.paky.endpoint;

import com.masa.endpoint.base.beans.AssociateCommand;
import com.masa.endpoint.base.beans.BaseAnswer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.Serializable;

import static com.masa.integration.paky.endpoint.BaseConrollerIT.BASE_BASE_URL;
import static io.micronaut.http.HttpRequest.POST;
@Singleton
public class BaseActions {

    @Inject
    @Client("/")
    HttpClient client;

    public HttpResponse<BaseAnswer> register() {
        return postCommand(BASE_BASE_URL + "register", "{}");
    }

    public HttpResponse<BaseAnswer> assign(BaseAnswer base) {
        return  postCommand(BASE_BASE_URL+ base.getBase().getBaseId() + "/assign",new AssociateCommand("customer1"));
    }

    public HttpResponse<BaseAnswer> search(BaseAnswer base) {
        return client.toBlocking().exchange(BASE_BASE_URL + base.getBase().getBaseId(), BaseAnswer.class);
    }

    public HttpResponse<BaseAnswer> postCommand(String uri, Serializable command) {
        return client.toBlocking().exchange(POST(uri, command), BaseAnswer.class);
    }
}
