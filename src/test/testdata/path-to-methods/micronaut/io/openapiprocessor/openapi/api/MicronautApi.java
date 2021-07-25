package io.openapiprocessor.openapi.api;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

public interface FooApi {

    @Get(uri = "/foo")
    void getFoo();

    @Post(uri = "/foo")
    void postFoo();

}
