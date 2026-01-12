package io.cmartinezs.keygo.run.config;

import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import io.cmartinezs.keygo.app.usecase.GetServiceInfoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration for use cases and dependency injection
 * Configuración de la aplicación para casos de uso e inyección de dependencias
 *
 * @author cmartinezs
 * @version 1.0
 */
@Configuration
@ComponentScan(basePackages = "io.cmartinezs.keygo.api")
public class ApplicationConfig {

  @Bean
  public GetServiceInfoUseCase getServiceInfoUseCase(ServiceInfoProvider serviceInfoProvider) {
    return new GetServiceInfoUseCase(serviceInfoProvider);
  }
}

