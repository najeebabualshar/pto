package gov.pto.aps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import gov.pto.aps.services.Imp.ApsApiServiceImp;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

		ApsApiServiceImp apsApiServiceImp = context.getBean(ApsApiServiceImp.class);

		apsApiServiceImp.start();
	}

}