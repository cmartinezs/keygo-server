package io.cmartinezs.keygo.run.config;

import io.cmartinezs.keygo.run.aop.NotLog;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
@NotLog
@Configuration
public class OpenApiConfig {

  /* Name of the Bearer security scheme used in OpenAPI spec */
  private static final String SECURITY_SCHEME_NAME = "BearerAuth";

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
        ? "🔒 Protected endpoints require `Authorization: Bearer <jwt>`."
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

    SecurityScheme bearerScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("Bearer JWT token issued by KeyGo OAuth2/OIDC endpoints.");

    return new OpenAPI()
        .info(info)
        .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme));
  }

  /**
   * Grouped API for Platform endpoints (service info, response codes, platform stats).
   * <p>Grupo de API para endpoints de plataforma: info del servicio, códigos de respuesta y
   * estadísticas de plataforma.
   *
   * @return {@link GroupedOpenApi} bean for platform group
   */
  @Bean
  public GroupedOpenApi platformGroup() {
    return GroupedOpenApi.builder()
        .group("1-platform")
        .displayName("🏠 Platform")
        .pathsToMatch(
            "/api/v1/service/**",
            "/api/v1/response-codes/**",
            "/api/v1/platform/**")
        .build();
  }

  /**
   * Grouped API for Tenant management endpoints (CRUD only — no OAuth2/OIDC/auth paths).
   * <p>Grupo de API para endpoints de gestión de tenants (solo CRUD — sin OAuth2/OIDC/auth).
   *
   * @return {@link GroupedOpenApi} bean for tenants group
   */
  @Bean
  public GroupedOpenApi tenantsGroup() {
    return GroupedOpenApi.builder()
        .group("2-tenants")
        .displayName("🏢 Tenants")
        .pathsToMatch("/api/v1/tenants/**")
        .pathsToExclude(
            "/api/v1/tenants/*/apps/**",
            "/api/v1/tenants/*/users/**",
            "/api/v1/tenants/*/.well-known/**",
            "/api/v1/tenants/*/oauth2/**",
            "/api/v1/tenants/*/account/**",
            "/api/v1/tenants/*/userinfo",
            "/api/v1/tenants/*/memberships/**")
        .build();
  }

  /**
   * Grouped API for Client Application management endpoints
   * (CRUD, secret rotation, self-registration).
   * <p>Billing ({@code /billing/**}) and app roles ({@code /roles/**}) are intentionally
   * excluded — see groups {@code 8-billing} and {@code 6-memberships} respectively.
   * <p>Grupo de API para endpoints de aplicaciones cliente: CRUD, rotación de secreto y
   * auto-registro. Billing y roles de app se exponen en sus grupos dedicados.
   *
   * @return {@link GroupedOpenApi} bean for client apps group
   */
  @Bean
  public GroupedOpenApi clientAppsGroup() {
    return GroupedOpenApi.builder()
        .group("3-client-apps")
        .displayName("📦 Client Apps")
        .pathsToMatch("/api/v1/tenants/*/apps/**")
        .pathsToExclude(
            "/api/v1/tenants/*/apps/*/billing/**",
            "/api/v1/tenants/*/apps/*/roles/**")
        .build();
  }

  /**
   * Grouped API for User management endpoints and self-service account profile.
   * <p>Grupo de API para gestión de usuarios y perfil propio del usuario.
   *
   * @return {@link GroupedOpenApi} bean for users group
   */
  @Bean
  public GroupedOpenApi usersGroup() {
    return GroupedOpenApi.builder()
        .group("4-users")
        .displayName("👤 Users")
        .pathsToMatch(
            "/api/v1/tenants/*/users/**",
            "/api/v1/platform/users/**",
            "/api/v1/tenants/*/account/profile")
        .build();
  }

  /**
   * Grouped API for self-service Account Settings endpoints: password management,
   * sessions, notification preferences, access info.
   * <p>Grupo de API para endpoints de auto-gestión de cuenta: contraseñas,
   * sesiones, preferencias de notificación, acceso.
   *
   * @return {@link GroupedOpenApi} bean for account settings group
   */
  @Bean
  public GroupedOpenApi accountSettingsGroup() {
    return GroupedOpenApi.builder()
        .group("4b-account")
        .displayName("⚙️ Account Settings")
        .pathsToMatch("/api/v1/tenants/*/account/**")
        .pathsToExclude(
            "/api/v1/tenants/*/account/profile",
            "/api/v1/tenants/*/account/login")
        .build();
  }

  /**
   * Grouped API for OAuth2 Authorization Code + PKCE flow, token exchange,
   * refresh token rotation, client_credentials grant and token revocation (RFC 7009).
   * <p>Grupo de API para el flujo OAuth2, intercambio de tokens y revocación.
   *
   * @return {@link GroupedOpenApi} bean for OAuth2 group
   */
  @Bean
  public GroupedOpenApi oauth2Group() {
    return GroupedOpenApi.builder()
        .group("5-oauth2")
        .displayName("🔐 OAuth2 / OIDC")
        .pathsToMatch(
            "/api/v1/tenants/*/oauth2/**",
            "/api/v1/tenants/*/account/login",
            "/api/v1/tenants/*/userinfo",
            "/api/v1/tenants/*/.well-known/**")
        .build();
  }

  /**
   * Grouped API for Membership management (grant/revoke user access to applications)
   * and App Role management (define available roles per client app).
   * <p>Grupo de API para gestión de membresías (asignar/revocar acceso de usuarios a apps)
   * y gestión de roles de app (definir roles disponibles por aplicación cliente).
   *
   * @return {@link GroupedOpenApi} bean for memberships group
   */
  @Bean
  public GroupedOpenApi membershipsGroup() {
    return GroupedOpenApi.builder()
        .group("6-memberships")
        .displayName("🔗 Memberships")
        .pathsToMatch(
            "/api/v1/tenants/*/memberships/**",
            "/api/v1/tenants/*/apps/*/roles/**")
        .build();
  }

  /**
   * Grouped API for Admin-only endpoints (dashboard, platform operations).
   * <p>Grupo de API para endpoints exclusivos de administrador global: dashboard completo
   * ({@code GET /api/v1/admin/platform/dashboard}). Todos requieren
   * {@code Authorization: Bearer <jwt>} con rol {@code ADMIN}.
   *
   * @return {@link GroupedOpenApi} bean for admin group
   */
  @Bean
  public GroupedOpenApi adminGroup() {
    return GroupedOpenApi.builder()
        .group("7-admin")
        .displayName("🛡️ Admin")
        .pathsToMatch("/api/v1/admin/**")
        .build();
  }

  /**
   * Grouped API for Billing endpoints: contracts (onboarding top-level) and tenant/app-level
   * subscriptions, invoices and billing catalog.
   * <p>Grupo de API para endpoints de billing: contratos de onboarding ({@code /api/v1/billing/**}),
   * catálogo de planes, suscripciones y facturas ({@code /api/v1/tenants/*&#47;apps/*&#47;billing/**}).
   *
   * @return {@link GroupedOpenApi} bean for billing group
   */
  @Bean
  public GroupedOpenApi billingGroup() {
    return GroupedOpenApi.builder()
        .group("8-billing")
        .displayName("💳 Billing")
        .pathsToMatch(
            "/api/v1/billing/**",
            "/api/v1/platform/billing/**",
            "/api/v1/tenants/*/apps/*/billing/**")
        .build();
  }

  /**
   * Global OpenAPI customizer that adds standard server-error responses to every operation.
   * <p>Agrega automáticamente las respuestas 500 y 503 a todos los endpoints para evitar
   * repetirlas en cada controller.
   *
   * @return the customizer bean
   */
  @Bean
  public OpenApiCustomizer globalErrorResponsesCustomizer() {
    Schema<?> errorSchema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
    Content errorContent = new Content().addMediaType(
        "application/json", new MediaType().schema(errorSchema));

    ApiResponse response500 = new ApiResponse()
        .description("Internal server error — code: OPERATION_FAILED")
        .content(errorContent);
    ApiResponse response503 = new ApiResponse()
        .description("External service unavailable — code: EXTERNAL_SERVICE_ERROR")
        .content(errorContent);

    return openApi -> openApi.getPaths().values().forEach(pathItem ->
        pathItem.readOperations().forEach(operation -> {
          var responses = operation.getResponses();
          responses.addApiResponse("500", response500);
          responses.addApiResponse("503", response503);
        })
    );
  }

  /**
   * Registers the {@link SnakeCaseModelConverter} so SpringDoc autodiscovers it
   * and renames all camelCase schema properties to snake_case in the OpenAPI spec.
   * This mirrors the global Jackson 3 {@code PropertyNamingStrategies.SNAKE_CASE}
   * setting applied at runtime via {@code JsonMapperBuilderCustomizer}.
   *
   * @return the model converter bean
   */
  @Bean
  public ModelConverter snakeCaseModelConverter() {
    return new SnakeCaseModelConverter();
  }
}



