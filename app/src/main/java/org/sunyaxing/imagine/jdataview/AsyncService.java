package org.sunyaxing.imagine.jdataview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
    @Autowired
    private TestService2 testService2;

    @Async
    public void async() {
        System.out.println("async");
        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("async end");
        testService2.test2();
    }
}
