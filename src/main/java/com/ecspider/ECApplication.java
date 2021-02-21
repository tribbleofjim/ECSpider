package com.ecspider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("org.webmaple.worker")
public class ECApplication {
    public static void main(String[] args) {
        SpringApplication.run(ECApplication.class, args);
    }
}
