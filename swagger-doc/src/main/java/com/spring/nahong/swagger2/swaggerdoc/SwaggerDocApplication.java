package com.spring.nahong.swagger2.swaggerdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
//@EnableEurekaClient
public class SwaggerDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaggerDocApplication.class, args);
    }
}
