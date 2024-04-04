package studio.giangbb.scylladbdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"studio.giangbb.scylladbdemo"})
public class ScylladbDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScylladbDemoApplication.class, args);
	}

}
