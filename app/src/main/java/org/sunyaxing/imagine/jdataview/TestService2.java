package org.sunyaxing.imagine.jdataview;

import org.springframework.stereotype.Service;

@Service
public class TestService2 {
    public String test() {
        test2();
        return "test2";
    }

    public String test2() {
        return "test2";
    }
}
