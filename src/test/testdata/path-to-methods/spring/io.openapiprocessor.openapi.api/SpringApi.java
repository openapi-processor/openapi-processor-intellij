package io.openapiprocessor.openapi.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface FooApi {

    @GetMapping(path = "/foo")
    void getFoo();

    @PostMapping(path = "/foo")
    void postFoo();

}
