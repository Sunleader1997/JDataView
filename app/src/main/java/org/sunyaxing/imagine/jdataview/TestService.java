package org.sunyaxing.imagine.jdataview;

import org.springframework.stereotype.Service;

@Service
public class TestService {
    public String test(String demo1) {
        test2();
        return "test";
    }

    public String test2() {
        return "test";
    }
}
