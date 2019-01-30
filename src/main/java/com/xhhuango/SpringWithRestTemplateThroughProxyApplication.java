package com.xhhuango;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringWithRestTemplateThroughProxyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringWithRestTemplateThroughProxyApplication.class, args);
    }
}
