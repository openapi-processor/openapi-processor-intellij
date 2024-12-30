package io.openapiprocessor.openapi.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public interface FooApi {

    @DeleteMapping(path = "/foo")
    void deleteFoo();

    @GetMapping(path = "/foo")
    void getFoo();

    @RequestMapping(path = "/foo", method = RequestMethod.HEAD)
    void headFoo();

    @PatchMapping(path = "/foo")
    void patchFoo();

    @PostMapping(path = "/foo")
    void postFoo();

    @PutMapping(path = "/foo")
    void putFoo();

    @RequestMapping(path = "/foo", method = RequestMethod.TRACE)
    void traceFoo();

}
