package io.cmartinezs.keygo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * @author cmartinezs
 * @version 1.0
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class KeygoApplication {
  public static void main(String[] args) {
    new SpringApplication(KeygoApplication.class).run(args);
  }
}

