package com.cci.rest.service;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableScheduling
@ComponentScan
public class WebApplication  extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}
	
	 
	 void addCorsMappings(CorsRegistry registry) 
	 {
		 registry.addMapping("/**")
	             .allowedOrigins("*")
	             .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
	 }
	 
	@Bean
	 FilterRegistrationBean simpleCorsFilter() 
	 {
		 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

	     CorsConfiguration config = new CorsConfiguration();
	     config.setAllowCredentials(false);
	     
	     // *** URL below needs to match the Vue client URL and port ***
	     config.setAllowedOrigins(Collections.singletonList("*"));
	     config.setAllowedMethods(Collections.singletonList("*"));
	     config.setAllowedHeaders(Collections.singletonList("*"));
	     source.registerCorsConfiguration("/**", config);
	     FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
	     bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
	     return bean;
	    }

}
