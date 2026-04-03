package io.cmartinezs.keygo.run.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.JwksBuilderPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.app.auth.usecase.AuthenticateUserForAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.ExchangeAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.GetJwksUseCase;
import io.cmartinezs.keygo.app.auth.usecase.GetOidcConfigurationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.GetUserInfoUseCase;
import io.cmartinezs.keygo.app.auth.usecase.InitiateAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueClientCredentialsTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.auth.usecase.OpenSessionUseCase;
import io.cmartinezs.keygo.app.auth.usecase.RotateRefreshTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.TerminateSessionUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientSecretEncoderPort;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.GetClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ResolveClientAppForAuthorizationUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateClientSecretUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.UpdateClientAppUseCase;
import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.membership.usecase.AssignRoleParentUseCase;
import io.cmartinezs.keygo.app.membership.usecase.CreateAppRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RemoveRoleParentUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort;
import io.cmartinezs.keygo.app.platform.port.PlatformStatsPort;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformDashboardUseCase;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformStatsUseCase;
import io.cmartinezs.keygo.app.platform.usecase.GetServiceInfoUseCase;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.tenant.usecase.ActivateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.ListTenantsUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.usecase.CreateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.RegisterTenantUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResendVerificationEmailUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetUserPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ValidateUserCredentialsUseCase;
import io.cmartinezs.keygo.app.user.usecase.VerifyEmailUseCase;
import io.cmartinezs.keygo.app.user.usecase.ChangePasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUserSessionsUseCase;
import io.cmartinezs.keygo.app.user.usecase.RevokeUserSessionUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserAccessUseCase;
import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.app.user.usecase.GetUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserProfileUseCase;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.usecase.CreateAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResendContractVerificationUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResumeContractOnboardingUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.invoice.usecase.ListAppInvoicesUseCase;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.usecase.CancelAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.GetAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.app.billing.usage.usecase.CheckAppEntitlementUseCase;
import io.cmartinezs.keygo.run.config.properties.KeyGoBillingProperties;
import io.cmartinezs.keygo.infra.email.SmtpEmailNotificationAdapter;
import org.springframework.mail.javamail.JavaMailSender;
import io.cmartinezs.keygo.run.clientapp.BCryptClientSecretEncoder;
import io.cmartinezs.keygo.run.clientapp.UuidClientCredentialGenerator;
import io.cmartinezs.keygo.run.config.auth.SystemClockProvider;
import io.cmartinezs.keygo.run.user.BCryptPasswordHasher;
import io.cmartinezs.keygo.infra.auth.jwt.RsaJwtTokenSigner;
import io.cmartinezs.keygo.infra.auth.jwt.RsaJwtTokenVerifier;
import io.cmartinezs.keygo.infra.auth.jwt.StandardTokenClaimsFactory;
import io.cmartinezs.keygo.infra.auth.jwks.JwkSetBuilder;
import io.cmartinezs.keygo.run.filter.RequestTracingFilter;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;

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
  public ActivateTenantUseCase activateTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    return new ActivateTenantUseCase(tenantRepositoryPort);
  }

  @Bean
  public ListTenantsUseCase listTenantsUseCase(TenantRepositoryPort tenantRepositoryPort) {
    return new ListTenantsUseCase(tenantRepositoryPort);
  }

  @Bean
  public GetPlatformStatsUseCase getPlatformStatsUseCase(PlatformStatsPort platformStatsPort) {
    return new GetPlatformStatsUseCase(platformStatsPort);
  }

  @Bean
  public GetPlatformDashboardUseCase getPlatformDashboardUseCase(
      PlatformDashboardPort platformDashboardPort,
      ServiceInfoProvider serviceInfoProvider) {
    return new GetPlatformDashboardUseCase(platformDashboardPort, serviceInfoProvider);
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
  public EmailNotificationPort emailNotificationPort(
      JavaMailSender mailSender,
      @Value("${keygo.mail.from:noreply@keygo.example.com}") String fromAddress,
      @Value("${keygo.mail.app-name:KeyGo}") String appName) {
    return new SmtpEmailNotificationAdapter(mailSender, fromAddress, appName);
  }

  @Bean
  public RegisterTenantUserUseCase registerTenantUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    return new RegisterTenantUserUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, userRepositoryPort,
        passwordHasherPort, emailVerificationRepositoryPort, emailNotificationPort);
  }

  @Bean
  public VerifyEmailUseCase verifyEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort) {
    return new VerifyEmailUseCase(
        tenantRepositoryPort, clientAppRepositoryPort,
        userRepositoryPort, emailVerificationRepositoryPort);
  }

  @Bean
  public ResendVerificationEmailUseCase resendVerificationEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort,
      EmailNotificationPort emailNotificationPort,
      ClockPort clockPort) {
    return new ResendVerificationEmailUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, userRepositoryPort,
        emailVerificationRepositoryPort, emailNotificationPort, clockPort);
  }

  @Bean
  public CreateAppRoleUseCase createAppRoleUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new CreateAppRoleUseCase(tenantRepositoryPort, clientAppRepositoryPort, appRoleRepositoryPort);
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
  public ListAppRolesUseCase listAppRolesUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new ListAppRolesUseCase(tenantRepositoryPort, clientAppRepositoryPort, appRoleRepositoryPort);
  }

  @Bean
  public AssignRoleParentUseCase assignRoleParentUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort,
      AppRoleHierarchyPort appRoleHierarchyPort) {
    return new AssignRoleParentUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, appRoleRepositoryPort, appRoleHierarchyPort);
  }

  @Bean
  public RemoveRoleParentUseCase removeRoleParentUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort,
      AppRoleHierarchyPort appRoleHierarchyPort) {
    return new RemoveRoleParentUseCase(
        tenantRepositoryPort, appRoleRepositoryPort, appRoleHierarchyPort);
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

  // ─── Fase 6: Token signing & OIDC metadata ────────────────────────────────

  @Bean
  public TokenSignerPort tokenSignerPort() {
    return new RsaJwtTokenSigner();
  }

  @Bean
  public TokenClaimsFactoryPort tokenClaimsFactoryPort() {
    return new StandardTokenClaimsFactory();
  }

  @Bean
  public JwksBuilderPort jwksBuilderPort() {
    return new JwkSetBuilder();
  }

  @Bean
  public IssueTokensUseCase issueTokensUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      TokenSignerPort tokenSignerPort,
      TokenClaimsFactoryPort tokenClaimsFactoryPort,
      ClockPort clockPort) {
    return new IssueTokensUseCase(signingKeyRepositoryPort, tokenSignerPort, tokenClaimsFactoryPort, clockPort);
  }

  @Bean
  public GetJwksUseCase getJwksUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      JwksBuilderPort jwksBuilderPort) {
    return new GetJwksUseCase(signingKeyRepositoryPort, jwksBuilderPort);
  }

  @Bean
  public GetOidcConfigurationUseCase getOidcConfigurationUseCase(
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}") String issuerBaseUrl) {
    return new GetOidcConfigurationUseCase(issuerBaseUrl);
  }

  // ─── Fase 7: Refresh tokens, sesiones, userinfo ───────────────────────────

  @Bean
  public AccessTokenVerifierPort accessTokenVerifierPort() {
    return new RsaJwtTokenVerifier();
  }

  @Bean
  public OpenSessionUseCase openSessionUseCase(
      SessionRepositoryPort sessionRepositoryPort) {
    return new OpenSessionUseCase(sessionRepositoryPort);
  }

  @Bean
  public TerminateSessionUseCase terminateSessionUseCase(
      SessionRepositoryPort sessionRepositoryPort,
      RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
    return new TerminateSessionUseCase(sessionRepositoryPort, refreshTokenRepositoryPort);
  }

  @Bean
  public RotateRefreshTokenUseCase rotateRefreshTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepositoryPort,
      SessionRepositoryPort sessionRepositoryPort,
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      TokenSignerPort tokenSignerPort,
      TokenClaimsFactoryPort tokenClaimsFactoryPort,
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      ClockPort clockPort,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}") String issuerBaseUrl) {
    return new RotateRefreshTokenUseCase(
        refreshTokenRepositoryPort, sessionRepositoryPort,
        signingKeyRepositoryPort, tokenSignerPort, tokenClaimsFactoryPort,
        tenantRepositoryPort, clientAppRepositoryPort, userRepositoryPort,
        membershipRepositoryPort, clockPort, issuerBaseUrl);
  }

  @Bean
  public RevokeTokenUseCase revokeTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
    return new RevokeTokenUseCase(refreshTokenRepositoryPort);
  }

  @Bean
  public GetUserInfoUseCase getUserInfoUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      UserRepositoryPort userRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort) {
    return new GetUserInfoUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        userRepositoryPort, tenantRepositoryPort);
  }

  // ─── Account Settings self-service ───────────────────────────────────────

  @Bean
  public ChangePasswordUseCase changePasswordUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    return new ChangePasswordUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
  }

  @Bean
  public ListUserSessionsUseCase listUserSessionsUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      SessionRepositoryPort sessionRepositoryPort) {
    return new ListUserSessionsUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, sessionRepositoryPort);
  }

  @Bean
  public RevokeUserSessionUseCase revokeUserSessionUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      SessionRepositoryPort sessionRepositoryPort,
      RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
    return new RevokeUserSessionUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, sessionRepositoryPort, refreshTokenRepositoryPort);
  }

  @Bean
  public GetNotificationPreferencesUseCase getNotificationPreferencesUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      NotificationPreferencesRepositoryPort notificationPreferencesRepositoryPort) {
    return new GetNotificationPreferencesUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, notificationPreferencesRepositoryPort);
  }

  @Bean
  public UpdateNotificationPreferencesUseCase updateNotificationPreferencesUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      NotificationPreferencesRepositoryPort notificationPreferencesRepositoryPort) {
    return new UpdateNotificationPreferencesUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, notificationPreferencesRepositoryPort);
  }

  @Bean
  public GetUserAccessUseCase getUserAccessUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    return new GetUserAccessUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        tenantRepositoryPort, membershipRepositoryPort, clientAppRepositoryPort);
  }

  // ─── Perfil de usuario self-service ──────────────────────────────────────

  @Bean
  public GetUserProfileUseCase getUserProfileUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      UserRepositoryPort userRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort) {
    return new GetUserProfileUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        userRepositoryPort, tenantRepositoryPort);
  }

  @Bean
  public UpdateUserProfileUseCase updateUserProfileUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      UserRepositoryPort userRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort) {
    return new UpdateUserProfileUseCase(
        signingKeyRepositoryPort, accessTokenVerifierPort,
        userRepositoryPort, tenantRepositoryPort);
  }

  // ─── Fase 8: Client Credentials grant ────────────────────────────────────

  @Bean
  public IssueClientCredentialsTokenUseCase issueClientCredentialsTokenUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ClientSecretEncoderPort clientSecretEncoderPort,
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      TokenSignerPort tokenSignerPort,
      TokenClaimsFactoryPort tokenClaimsFactoryPort,
      ClockPort clockPort,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}") String issuerBaseUrl) {
    return new IssueClientCredentialsTokenUseCase(
        tenantRepositoryPort,
        clientAppRepositoryPort,
        clientSecretEncoderPort,
        signingKeyRepositoryPort,
        tokenSignerPort,
        tokenClaimsFactoryPort,
        clockPort,
        issuerBaseUrl);
  }

  // ─── Billing: Catálogo ────────────────────────────────────────────────────

  @Bean
  public GetAppPlanCatalogUseCase getAppPlanCatalogUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new GetAppPlanCatalogUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Bean
  public GetAppPlanUseCase getAppPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new GetAppPlanUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Bean
  public CreateAppPlanUseCase createAppPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new CreateAppPlanUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  // ─── Billing: Contratación ────────────────────────────────────────────────

  @Bean
  public CreateAppContractUseCase createAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      EmailNotificationPort emailNotificationPort,
      KeyGoBillingProperties billingProperties) {
    return new CreateAppContractUseCase(
        contractRepo, versionRepo, emailNotificationPort,
        billingProperties.getContractExpiryHours(),
        billingProperties.getVerificationCodeExpiryMinutes());
  }

  @Bean
  public VerifyContractEmailUseCase verifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepo,
      ContractorRepositoryPort contractorRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort,
      PasswordHasherPort passwordHasherPort,
      EmailNotificationPort emailNotificationPort) {
    return new VerifyContractEmailUseCase(
        contractRepo, clientAppRepositoryPort, userRepo, contractorRepositoryPort,
        membershipRepositoryPort, appRoleRepositoryPort, passwordHasherPort, emailNotificationPort);
  }

  @Bean
  public GetAppContractUseCase getAppContractUseCase(AppContractRepositoryPort contractRepo) {
    return new GetAppContractUseCase(contractRepo);
  }

  @Bean
  public MockApprovePaymentUseCase mockApprovePaymentUseCase(
      AppContractRepositoryPort contractRepo,
      KeyGoBillingProperties billingProperties) {
    return new MockApprovePaymentUseCase(contractRepo, billingProperties.isMockPaymentEnabled());
  }

  @Bean
  public ActivateAppContractUseCase activateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppSubscriptionRepositoryPort subscriptionRepo,
      InvoiceRepositoryPort invoiceRepo) {
    return new ActivateAppContractUseCase(
        contractRepo, versionRepo, billingOptionRepo, subscriptionRepo, invoiceRepo);
  }

  @Bean
  public ResumeContractOnboardingUseCase resumeContractOnboardingUseCase(
      AppContractRepositoryPort contractRepo) {
    return new ResumeContractOnboardingUseCase(contractRepo);
  }

  @Bean
  public ResendContractVerificationUseCase resendContractVerificationUseCase(
      AppContractRepositoryPort contractRepo,
      EmailNotificationPort emailNotificationPort,
      KeyGoBillingProperties billingProperties) {
    return new ResendContractVerificationUseCase(
        contractRepo, emailNotificationPort,
        billingProperties.getVerificationCodeExpiryMinutes());
  }

  // ─── Billing: Suscripción ─────────────────────────────────────────────────

  @Bean
  public GetAppSubscriptionUseCase getAppSubscriptionUseCase(
      AppSubscriptionRepositoryPort subscriptionRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new GetAppSubscriptionUseCase(subscriptionRepo, versionRepo, entitlementRepo);
  }

  @Bean
  public CancelAppSubscriptionUseCase cancelAppSubscriptionUseCase(
      AppSubscriptionRepositoryPort subscriptionRepo) {
    return new CancelAppSubscriptionUseCase(subscriptionRepo);
  }

  // ─── Billing: Facturación ─────────────────────────────────────────────────

  @Bean
  public ListAppInvoicesUseCase listAppInvoicesUseCase(InvoiceRepositoryPort invoiceRepo) {
    return new ListAppInvoicesUseCase(invoiceRepo);
  }

  // ─── Billing: Uso ─────────────────────────────────────────────────────────

  @Bean
  public CheckAppEntitlementUseCase checkAppEntitlementUseCase(
      AppSubscriptionRepositoryPort subscriptionRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo,
      UsageCounterRepositoryPort usageRepo) {
    return new CheckAppEntitlementUseCase(subscriptionRepo, versionRepo, entitlementRepo, usageRepo);
  }

  // ─── Trazabilidad: RequestTracingFilter ──────────────────────────────────

  /**
   * Registers {@link RequestTracingFilter} at the highest possible precedence so that every
   * incoming request gets a {@code traceId} in the MDC before any other filter runs.
   *
   * <p>The filter is mapped to {@code /*} to cover all paths, including actuator and public
   * endpoints. The trace ID is also added as the {@code X-Trace-ID} response header.
   */
  @Bean
  public FilterRegistrationBean<RequestTracingFilter> requestTracingFilterRegistration() {
    FilterRegistrationBean<RequestTracingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RequestTracingFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.addUrlPatterns("/*");
    registration.setName("requestTracingFilter");
    return registration;
  }

    @Bean
    JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
            // Deserializa snake_case (OAuth2) y serializa también en snake_case — globalmente.
            // Los DTOs con @JsonProperty explícito mantienen su nombre definido.
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

            // Robustez ante cambios en payloads (típico en integraciones)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            // Interoperabilidad: acepta variaciones de case además de snake_case
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

            // Payloads limpios
            .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))

            // Coherencia entre ambientes
            .defaultTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
