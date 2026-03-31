package io.cmartinezs.keygo.run.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for KeyGo bootstrap settings.({@code /api/v1/tenants/*&#47;apps/*&#47;billing/**}).
 * <p>Propiedades de configuración para ajustes de arranque de KeyGo.
 * @author cmartinezs
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "keygo.bootstrap")
@Validated
@Getter
@Setter
public class KeyGoBootstrapProperties {

  /* Whether bootstrap is enabled. Default is true.
   * Si el arranque está habilitado. Por defecto es true. */
  private boolean enabled = true;

  /**
   * Roles granted in bypass mode (when {@code enabled=false}).
   * Written without the {@code ROLE_} prefix — the filter adds it automatically.
   * Configurable via {@code keygo.bootstrap.bypass-roles} in application YAML.
   * <p>Roles otorgados en modo bypass (cuando {@code enabled=false}).
   * Se escriben sin el prefijo {@code ROLE_} — el filtro lo agrega automáticamente.
   */
  private List<String> bypassRoles = List.of("ADMIN", "ADMIN_TENANT", "USER");

  /* API path prefix that requires authentication.
   * Prefijo de ruta de API que requiere autenticación.
   * Must be configured in application.yml (e.g., "/api/")
   * Debe ser configurado en application.yml (ej., "/api/") */
  private String apiPathPrefix;

  /* Actuator path prefix that is public.
   * Prefijo de ruta de actuator que es público.
   * Must be configured in application.yml (e.g., "/actuator/")
   * Debe ser configurado en application.yml (ej., "/actuator/") */
  private String actuatorPathPrefix;

  /* Service info path prefix that is public.
   * Prefijo de ruta de información de servicio que es público.
   * Must be configured in application.yml (e.g., "/service/info")
   * Debe ser configurado en application.yml (ej., "/service/info") */
  private String serviceInfoPathPrefix;

  /* Swagger UI path prefix that is public.
   * Prefijo de ruta de Swagger UI que es público. */
  private String swaggerUiPathPrefix;

  /* API docs path prefix that is public.
   * Prefijo de ruta de API docs que es público. */
  private String apiDocsPathPrefix;

  /* Well-known path prefix that is public (OIDC discovery, JWKS).
   * Prefijo de ruta .well-known que es público (discovery OIDC, JWKS). */
  private String wellKnownPathPrefix;

  /* UserInfo path suffix that is public (validated via Bearer token).
   * Sufijo de ruta de userinfo que es público (validado vía Bearer token). */
  private String userInfoPathSuffix;

  /* Token revocation path suffix that is public (RFC 7009).
   * Sufijo de ruta de revocación que es público (RFC 7009). */
  private String revocationPathSuffix;

  /* Self-registration path suffix that is public.
   * Sufijo de ruta de auto-registro que es público. */
  private String registerPathSuffix;

  /* Email verification path suffix that is public.
   * Sufijo de ruta de verificación de email que es público. */
  private String verifyEmailPathSuffix;

  /* Resend verification path suffix that is public.
   * Sufijo de ruta de reenvío de verificación que es público. */
  private String resendVerificationPathSuffix;

  /* Account profile path suffix that is public (validated via Bearer token).
   * Sufijo de ruta de perfil de cuenta que es público (validado vía Bearer token). */
  private String accountProfilePathSuffix;

  /* OAuth2 authorize path suffix that is public (browser navigates here to start the flow).
   * Sufijo de ruta authorize que es público (el navegador navega aquí para iniciar el flujo). */
  private String authorizePathSuffix;

  /* Account login path suffix that is public (user POSTs credentials during OAuth2 flow).
   * Sufijo de ruta login que es público (el usuario envía credenciales durante el flujo OAuth2). */
  private String loginPathSuffix;

  /* OAuth2 token path suffix that is public (code exchange, PKCE-protected).
   * Sufijo de ruta token que es pblico (canje de cdigo, protegido por PKCE). */
  private String tokenPathSuffix;

  /* Billing catalog path suffix that is public (plan catalog visible without auth).
   * Sufijo de ruta del catálogo de billing que es público. */
  private String billingCatalogPathSuffix;

  /* Billing contracts path suffix that is public (contracting flow).
   * Sufijo de ruta de contratos de billing que es público. */
  private String billingContractsPathSuffix;

}


