package io.cmartinezs.keygo.run;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author cmartinezs
 * @version 1.0
 */
@SpringBootApplication
public class KeyGoRunner {
  public static void main(String[] args) {
    new SpringApplication(KeyGoRunner.class).run(args);
  }
}
