package api;

import org.springframework.web.bind.annotation.PostMapping;

public interface FooApi {

    @PostMapping(path = "/foo")
    Foo postFoo();

}
