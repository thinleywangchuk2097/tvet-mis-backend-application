package com.moesd.tvet.mis.backend.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;


@SpringBootApplication
public class TvetMisBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TvetMisBackendApplication.class, args);
	}
	
	@Bean
	public ObjectToJson objectToJson() {
		return new ObjectToJson();
	} 

}
