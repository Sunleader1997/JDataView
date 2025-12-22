package org.sunyaxing.imagine.jdataview;

import org.springframework.stereotype.Service;

@Service
public class TestService {
    public String test(String demo1) {
        test2();
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "test";
    }

    public String test2() {
        return "test";
    }
}
