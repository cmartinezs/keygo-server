package io.cmartinezs.keygo.run.config;

import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for KeyGo Server.
 * <p>Configuración de OpenAPI / Swagger para KeyGo Server.
 *
 * <p>La UI estará disponible en:
 * {@code http://localhost:8080/keygo-server/swagger-ui/index.html}
 *
 * @author cmartinezs
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

  /* Name of the API key security scheme used in OpenAPI spec */
  private static final String SECURITY_SCHEME_NAME = "AdminKeyAuth";

  /**
   * Global OpenAPI metadata: title, version, description, contact, license and security scheme.
   * <p>Metadatos globales de OpenAPI: título, versión, descripción, contacto, licencia y esquema
   * de seguridad.
   *
   * @param bootstrapProperties bootstrap properties to reflect enabled status in description
   * @return configured {@link OpenAPI} bean
   */
  @Bean
  public OpenAPI keyGoOpenAPI(KeyGoBootstrapProperties bootstrapProperties) {
    String securityNote = bootstrapProperties.isEnabled()
        ? "🔒 Protected endpoints require the `X-KEYGO-ADMIN` header."
        : "⚠️ Bootstrap security is disabled — all endpoints are public.";

    Info info = new Info()
        .title("KeyGo Server API")
        .version("1.0")
        .description(
            "Enterprise authentication service — open source alternative for centralized "
            + "identity, user and access management.\n\n" + securityNote)
        .contact(new Contact()
            .name("KeyGo Server")
            .url("https://github.com/cmartinezs/keygo-server"))
        .license(new License()
            .name("AGPL-3.0")
            .url("https://www.gnu.org/licenses/agpl-3.0.txt"));

    SecurityScheme adminKeyScheme = new SecurityScheme()
        .type(SecurityScheme.Type.APIKEY)
        .in(SecurityScheme.In.HEADER)
        .name("X-KEYGO-ADMIN")
        .description("Admin API key. Default dev value: `changeMe`. "
                     + "Set via `KEYGO_ADMIN_KEY` environment variable.");

    return new OpenAPI()
        .info(info)
        .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, adminKeyScheme));
  }

  /**
   * Grouped API for Platform endpoints (service info, response codes).
   * <p>Grupo de API para endpoints de plataforma.
   *
   * @return {@link GroupedOpenApi} bean for platform group
   */
  @Bean
  public GroupedOpenApi platformGroup() {
    return GroupedOpenApi.builder()
        .group("1-platform")
        .displayName("🏠 Platform")
        .pathsToMatch("/api/v1/service/**", "/api/v1/response-codes/**")
        .build();
  }

  /**
   * Grouped API for Tenant management endpoints.
   * <p>Grupo de API para endpoints de gestión de tenants.
   *
   * @return {@link GroupedOpenApi} bean for tenants group
   */
  @Bean
  public GroupedOpenApi tenantsGroup() {
    return GroupedOpenApi.builder()
        .group("2-tenants")
        .displayName("🏢 Tenants")
        .pathsToMatch("/api/v1/tenants/**")
        .pathsToExclude("/api/v1/tenants/*/apps/**", "/api/v1/tenants/*/users/**")
        .build();
  }

  /**
   * Grouped API for Client Application management endpoints.
   * <p>Grupo de API para endpoints de gestión de aplicaciones cliente.
   *
   * @return {@link GroupedOpenApi} bean for client apps group
   */
  @Bean
  public GroupedOpenApi clientAppsGroup() {
    return GroupedOpenApi.builder()
        .group("3-client-apps")
        .displayName("📦 Client Apps")
        .pathsToMatch("/api/v1/tenants/*/apps/**")
        .build();
  }

  /**
   * Grouped API for User management endpoints.
   * <p>Grupo de API para endpoints de gestión de usuarios.
   *
   * @return {@link GroupedOpenApi} bean for users group
   */
  @Bean
  public GroupedOpenApi usersGroup() {
    return GroupedOpenApi.builder()
        .group("4-users")
        .displayName("👤 Users")
        .pathsToMatch("/api/v1/tenants/*/users/**")
        .build();
  }
}



