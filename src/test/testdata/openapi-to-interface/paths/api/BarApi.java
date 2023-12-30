package api;

import org.springframework.web.bind.annotation.PostMapping;

public interface BarApi {

    @PostMapping(path = "/bar")
    Bar postBar();

}
