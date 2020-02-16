package com.tang.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ExampleService.class)
@EnableConfigurationProperties(ExampleServiceProperties.class)
public class ExampleAutoConfigure {

	@Autowired
	private ExampleServiceProperties properties;

	@Bean
	@ConditionalOnMissingClass
	@ConditionalOnProperty(prefix = "example.service", value = "enabled", havingValue = "true")
	public ExampleService exampleService() {
		return new ExampleService(properties.getPrefix(), properties.getSuffix());
	}
}
