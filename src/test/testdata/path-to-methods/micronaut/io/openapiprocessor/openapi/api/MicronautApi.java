package io.openapiprocessor.openapi.api;

import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Head;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Trace;

public interface FooApi {

    @Delete(uri = "/foo")
    void deleteFoo();

    @Get(uri = "/foo")
    void getFoo();

    @Head(uri = "/foo")
    void headFoo();

    @Patch(uri = "/foo")
    void patchFoo();

    @Post(uri = "/foo")
    void postFoo();

    @Put(uri = "/foo")
    void putFoo();

    @Trace(uri = "/foo")
    void traceFoo();

}
