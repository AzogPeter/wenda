package com.nowcoder;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Created by Azog on 2019/5/30.
 */
@SpringBootApplication
public class WendaApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WendaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WendaApplication.class, args);
    }
}
