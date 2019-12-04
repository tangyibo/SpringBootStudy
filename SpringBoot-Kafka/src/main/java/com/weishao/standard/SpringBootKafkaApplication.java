package com.weishao.standard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner;

@SpringBootApplication
public class SpringBootKafkaApplication {

	public static void main(String[] args)  {
		SpringApplication springApplication = new SpringApplication(SpringBootKafkaApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

}
