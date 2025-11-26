package com.example.gradproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class GradProjectBackend {

    public static void main(String[] args) {
        SpringApplication.run(GradProjectBackend.class, args);
    }

}
