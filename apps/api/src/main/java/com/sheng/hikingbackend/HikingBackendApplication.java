package com.sheng.hikingbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sheng.hikingbackend.mapper")
public class HikingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HikingBackendApplication.class, args);
	}

}
