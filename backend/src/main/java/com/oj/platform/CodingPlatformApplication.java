package com.oj.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CodingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodingPlatformApplication.class, args);
    }
}
