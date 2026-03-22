package io.cmartinezs.keygo.run.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.usecase.AuthenticateUserForAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.ExchangeAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.InitiateAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientSecretEncoderPort;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.GetClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ResolveClientAppForAuthorizationUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateClientSecretUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.UpdateClientAppUseCase;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.usecase.GetServiceInfoUseCase;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.usecase.CreateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetUserPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ValidateUserCredentialsUseCase;
import io.cmartinezs.keygo.run.clientapp.BCryptClientSecretEncoder;
import io.cmartinezs.keygo.run.clientapp.UuidClientCredentialGenerator;
import io.cmartinezs.keygo.run.config.auth.SystemClockProvider;
import io.cmartinezs.keygo.run.user.BCryptPasswordHasher;
import java.util.TimeZone;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;

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
  public ClientSecretEncoderPort clientSecretEncoderPort() {
    return new BCryptClientSecretEncoder();
  }

  @Bean
  public ClientCredentialGeneratorPort clientCredentialGeneratorPort() {
    return new UuidClientCredentialGenerator();
  }

  @Bean
  public CreateClientAppUseCase createClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ClientCredentialGeneratorPort credentialGenerator,
      ClientSecretEncoderPort secretEncoder) {
    return new CreateClientAppUseCase(tenantRepositoryPort, clientAppRepositoryPort, credentialGenerator, secretEncoder);
  }

  @Bean
  public ListClientAppsUseCase listClientAppsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new ListClientAppsUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public GetClientAppUseCase getClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new GetClientAppUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public UpdateClientAppUseCase updateClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new UpdateClientAppUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public RotateClientSecretUseCase rotateClientSecretUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ClientCredentialGeneratorPort credentialGenerator,
      ClientSecretEncoderPort secretEncoder) {
    return new RotateClientSecretUseCase(tenantRepositoryPort, clientAppRepositoryPort, credentialGenerator, secretEncoder);
  }

  @Bean
  public ResolveClientAppForAuthorizationUseCase resolveClientAppForAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new ResolveClientAppForAuthorizationUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public PasswordHasherPort passwordHasherPort() {
    return new BCryptPasswordHasher();
  }

  @Bean
  public CreateUserUseCase createUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    return new CreateUserUseCase(tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
  }

  @Bean
  public GetUserUseCase getUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    return new GetUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public ListUsersUseCase listUsersUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    return new ListUsersUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public UpdateUserUseCase updateUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    return new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public ResetUserPasswordUseCase resetUserPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    return new ResetUserPasswordUseCase(tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
  }

  @Bean
  public ValidateUserCredentialsUseCase validateUserCredentialsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    return new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
  }

  @Bean
  public CreateMembershipUseCase createMembershipUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new CreateMembershipUseCase(tenantRepositoryPort, membershipRepositoryPort, appRoleRepositoryPort);
  }

  @Bean
  public ListMembershipsUseCase listMembershipsUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    return new ListMembershipsUseCase(membershipRepositoryPort);
  }

  @Bean
  public RevokeMembershipUseCase revokeMembershipUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    return new RevokeMembershipUseCase(membershipRepositoryPort);
  }

  @Bean
  public ListAppRolesUseCase listAppRolesUseCase(AppRoleRepositoryPort appRoleRepositoryPort) {
    return new ListAppRolesUseCase(appRoleRepositoryPort);
  }

  @Bean
  public ClockPort clockPort() {
    return new SystemClockProvider();
  }

  @Bean
  public InitiateAuthorizationUseCase initiateAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new InitiateAuthorizationUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    return new AuthenticateUserForAuthorizationUseCase(
        tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
  }

  @Bean
  public IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase(
      AuthorizationCodeRepositoryPort authorizationCodeRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort,
      ClockPort clockPort) {
    return new IssueAuthorizationCodeUseCase(
        authorizationCodeRepositoryPort,
        clientAppRepositoryPort,
        userRepositoryPort,
        membershipRepositoryPort,
        tenantRepositoryPort,
        clockPort);
  }

  @Bean
  public ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase(
      AuthorizationCodeRepositoryPort authorizationCodeRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort,
      ClockPort clockPort) {
    return new ExchangeAuthorizationCodeUseCase(
        authorizationCodeRepositoryPort, clientAppRepositoryPort, tenantRepositoryPort, clockPort);
  }

    @Bean
    JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder

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
