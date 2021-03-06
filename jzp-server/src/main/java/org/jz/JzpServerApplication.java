package org.jz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * entrance of eureka server
 * @author Hongyi Zheng
 * @date 2018/2/8
 */
@EnableEurekaServer
@SpringBootApplication
public class JzpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JzpServerApplication.class, args);
	}
}
