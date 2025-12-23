package org.sunyaxing.imagine.jdataviewserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"org.sunyaxing.imagine.jdataviewserver.service.repository"})
public class JDataViewServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JDataViewServerApplication.class, args);
    }
}
