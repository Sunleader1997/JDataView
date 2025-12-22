package org.sunyaxing.imagine.jdataviewserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Callable;

@SpringBootApplication
@MapperScan({"org.sunyaxing.imagine.jdataviewserver.service.repository"})
public class JDataViewServerApplication implements Callable<Integer> {

    public static void main(String[] args) {
        SpringApplication.run(JDataViewServerApplication.class, args);
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
