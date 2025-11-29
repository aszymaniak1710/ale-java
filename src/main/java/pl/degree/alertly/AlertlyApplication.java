package pl.degree.alertly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlertlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertlyApplication.class, args);
	}

}
