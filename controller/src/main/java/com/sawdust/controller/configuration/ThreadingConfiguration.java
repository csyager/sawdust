package com.sawdust.controller.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;

public class ThreadingConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
