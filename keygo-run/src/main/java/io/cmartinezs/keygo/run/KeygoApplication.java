package io.cmartinezs.keygo.run;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author cmartinezs
 * @version 1.0
 */
@SpringBootApplication
public class KeygoApplication {
  public static void main(String[] args) {
    new SpringApplication(KeygoApplication.class).run(args);
  }
}

