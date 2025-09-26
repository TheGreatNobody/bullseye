package com.sun.av;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableEncryptableProperties
@EnableAsync
@SpringBootApplication
public class AsyncValidationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncValidationApplication.class, args);
	}

}
