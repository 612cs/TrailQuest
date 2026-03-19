package com.sheng.hikingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Configuration
public class JacksonLongToStringConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonLongToStringCustomizer() {
        return (builder) -> builder.serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
