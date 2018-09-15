package com.spring.nahong.turbine.clientturbinemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

@SpringBootApplication
@EnableTurbine
@EnableHystrixDashboard
public class ClientTurbineMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientTurbineMonitorApplication.class, args);
	}
}
