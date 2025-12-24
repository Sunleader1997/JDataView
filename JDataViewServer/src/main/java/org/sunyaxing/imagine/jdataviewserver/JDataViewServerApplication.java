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

    public static Mode applicationMode(String[] args) {
        try {
            for (String arg : args) {
                if ("--mode=cli".equals(arg)) {
                    return Mode.CLI;
                }
            }
        } catch (Exception e) {
            return Mode.WEB;
        }
        return Mode.WEB;
    }

    public enum Mode {
        WEB,
        CLI
    }
}
