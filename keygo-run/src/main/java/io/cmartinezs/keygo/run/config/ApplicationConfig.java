package io.cmartinezs.keygo.run.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.usecase.GetServiceInfoUseCase;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
import java.util.TimeZone;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Application configuration for use cases and dependency injection
 * Configuración de la aplicación para casos de uso e inyección de dependencias
 *
 * @author cmartinezs
 * @version 1.0
 */
@Configuration
@ComponentScan(basePackages = {
    "io.cmartinezs.keygo.api",
    "io.cmartinezs.keygo.supabase"
})
public class ApplicationConfig {

  @Bean
  public GetServiceInfoUseCase getServiceInfoUseCase(ServiceInfoProvider serviceInfoProvider) {
    return new GetServiceInfoUseCase(serviceInfoProvider);
  }

  @Bean
  public CreateTenantUseCase createTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    return new CreateTenantUseCase(tenantRepositoryPort);
  }

  @Bean
  public GetTenantBySlugUseCase getTenantBySlugUseCase(TenantRepositoryPort tenantRepositoryPort) {
    return new GetTenantBySlugUseCase(tenantRepositoryPort);
  }

  @Bean
  public SuspendTenantUseCase suspendTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    return new SuspendTenantUseCase(tenantRepositoryPort);
  }

    @Bean
    JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
            // Módulos explícitos (si los necesitas)
            .addModule(new JavaTimeModule())

            // Robustez ante cambios en payloads (típico en integraciones)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            // Interoperabilidad (opcional)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

            // Payloads limpios
            .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))

            // Coherencia entre ambientes
            .defaultTimeZone(TimeZone.getTimeZone("UTC"));
    }
}

