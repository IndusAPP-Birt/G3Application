package eus.birtlh.g3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class G3Application {

	public static void main(String[] args) {
		SpringApplication.run(G3Application.class, args);
	}

}
