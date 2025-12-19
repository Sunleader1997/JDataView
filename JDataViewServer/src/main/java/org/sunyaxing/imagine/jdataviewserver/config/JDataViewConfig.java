package org.sunyaxing.imagine.jdataviewserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@CrossOrigin
@Configuration
public class JDataViewConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
