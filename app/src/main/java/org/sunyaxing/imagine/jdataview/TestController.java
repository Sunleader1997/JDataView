package org.sunyaxing.imagine.jdataview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
    private TestService testService;
    @Autowired
    private TestService2 testService2;
    @Autowired
    private AsyncService asyncService;

    @GetMapping("/demo")
    public String demo(@RequestParam String name) {
        testService.test(name);
        asyncService.async();
        return testService2.test();
    }
}
