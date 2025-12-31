package com.smartlingo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smartlingo.mapper")
public class SmartLingoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartLingoApplication.class, args);
    }
}
