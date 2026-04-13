package io.cmartinezs.keygo.run.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
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
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.RotateRefreshTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.TerminateSessionUseCase;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.usecase.CreateAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResendContractVerificationUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResumeContractOnboardingUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorUserRepositoryPort;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.invoice.usecase.ListAppInvoicesUseCase;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.usecase.CancelAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.GetAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.app.billing.usage.usecase.CheckAppEntitlementUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.CancelPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.ListPlatformInvoicesUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
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
import io.cmartinezs.keygo.app.membership.usecase.ApproveMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RemoveRoleParentUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.TenantUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.AssignTenantRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.CreateTenantRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokePlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeTenantRoleUseCase;
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
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.platform.usecase.IssuePlatformTokensUseCase;
import io.cmartinezs.keygo.app.platform.usecase.RotatePlatformRefreshTokenUseCase;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.usecase.ActivatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ActivateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.AuthenticatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CheckPlatformUserEmailUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListPlatformUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ChangePasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ForgotPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserAccessUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUserSessionsUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.RecoverPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.RegisterTenantUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResendVerificationEmailUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetUserPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.RevokeUserSessionUseCase;
import io.cmartinezs.keygo.app.user.usecase.SendPasswordResetCodeUseCase;
import io.cmartinezs.keygo.app.user.usecase.SendPlatformPasswordResetCodeUseCase;
import io.cmartinezs.keygo.app.user.usecase.ForgotPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.RecoverPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ValidateUserCredentialsUseCase;
import io.cmartinezs.keygo.app.user.usecase.VerifyEmailUseCase;
import io.cmartinezs.keygo.infra.adapter.notification.EmailNotificationAdapter;
import io.cmartinezs.keygo.infra.auth.jwks.JwkSetBuilder;
import io.cmartinezs.keygo.infra.auth.jwt.RsaJwtTokenSigner;
import io.cmartinezs.keygo.infra.auth.jwt.RsaJwtTokenVerifier;
import io.cmartinezs.keygo.infra.auth.jwt.StandardTokenClaimsFactory;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoUiProperties;
import io.cmartinezs.keygo.run.aop.NotLog;
import io.cmartinezs.keygo.run.clientapp.UuidClientCredentialGenerator;
import io.cmartinezs.keygo.run.config.auth.SystemClockProvider;
import io.cmartinezs.keygo.run.config.properties.KeyGoBillingProperties;
import io.cmartinezs.keygo.run.config.properties.KeyGoPlatformProperties;
import io.cmartinezs.keygo.run.credential.BCryptCredentialEncoder;
import io.cmartinezs.keygo.app.platform.port.PlatformConfigPort;
import io.cmartinezs.keygo.api.shared.KeyGoLocaleResolver;
import io.cmartinezs.keygo.run.filter.LocaleContextFilter;
import io.cmartinezs.keygo.run.filter.RequestTracingFilter;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;

/**
 * Application configuration for use cases and dependency injection Configuración de la aplicación
 * para casos de uso e inyección de dependencias
 *
 * @author cmartinezs
 * @version 1.0
 */
@NotLog
@Configuration
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
      PlatformDashboardPort platformDashboardPort, ServiceInfoProvider serviceInfoProvider) {
    return new GetPlatformDashboardUseCase(platformDashboardPort, serviceInfoProvider);
  }

  @Bean
  public CredentialEncoderPort credentialEncoderPort() {
    return new BCryptCredentialEncoder();
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
      CredentialEncoderPort credentialEncoderPort) {
    return new CreateClientAppUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, credentialGenerator, credentialEncoderPort);
  }

  @Bean
  public ListClientAppsUseCase listClientAppsUseCase(
      TenantRepositoryPort tenantRepositoryPort, ClientAppRepositoryPort clientAppRepositoryPort) {
    return new ListClientAppsUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public GetClientAppUseCase getClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort, ClientAppRepositoryPort clientAppRepositoryPort) {
    return new GetClientAppUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public UpdateClientAppUseCase updateClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort, ClientAppRepositoryPort clientAppRepositoryPort) {
    return new UpdateClientAppUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public RotateClientSecretUseCase rotateClientSecretUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ClientCredentialGeneratorPort credentialGenerator,
      CredentialEncoderPort credentialEncoderPort) {
    return new RotateClientSecretUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, credentialGenerator, credentialEncoderPort);
  }

  @Bean
  public ResolveClientAppForAuthorizationUseCase resolveClientAppForAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort, ClientAppRepositoryPort clientAppRepositoryPort) {
    return new ResolveClientAppForAuthorizationUseCase(
        tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public CreateUserUseCase createUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    return new CreateUserUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
  }

  @Bean
  public GetUserUseCase getUserUseCase(
      TenantRepositoryPort tenantRepositoryPort, UserRepositoryPort userRepositoryPort) {
    return new GetUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public ListUsersUseCase listUsersUseCase(
      TenantRepositoryPort tenantRepositoryPort, UserRepositoryPort userRepositoryPort) {
    return new ListUsersUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public UpdateUserUseCase updateUserUseCase(
      TenantRepositoryPort tenantRepositoryPort, UserRepositoryPort userRepositoryPort) {
    return new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public ResetUserPasswordUseCase resetUserPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    return new ResetUserPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
  }

  @Bean
  public ValidateUserCredentialsUseCase validateUserCredentialsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      PlatformUserRepositoryPort platformUserRepositoryPort) {
    return new ValidateUserCredentialsUseCase(
        tenantRepositoryPort, userRepositoryPort, credentialEncoderPort, platformUserRepositoryPort);
  }

  @Bean
  public SuspendUserUseCase suspendUserUseCase(
      TenantRepositoryPort tenantRepositoryPort, UserRepositoryPort userRepositoryPort) {
    return new SuspendUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public ActivateUserUseCase activateUserUseCase(
      TenantRepositoryPort tenantRepositoryPort, UserRepositoryPort userRepositoryPort) {
    return new ActivateUserUseCase(tenantRepositoryPort, userRepositoryPort);
  }

  @Bean
  public EmailNotificationPort emailNotificationPort(
      TemplateEngine emailTemplateEngine,
      JavaMailSender mailSender,
      KeyGoUiProperties uiProperties,
      KeyGoEmailProperties emailProperties) {
    return new EmailNotificationAdapter(emailTemplateEngine, mailSender, uiProperties, emailProperties);
  }

  @Bean
  public RegisterTenantUserUseCase registerTenantUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    return new RegisterTenantUserUseCase(
        tenantRepositoryPort,
        clientAppRepositoryPort,
        userRepositoryPort,
        credentialEncoderPort,
        verificationCodeRepositoryPort,
        emailNotificationPort);
  }

  @Bean
  public VerifyEmailUseCase verifyEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort) {
    return new VerifyEmailUseCase(
        tenantRepositoryPort, clientAppRepositoryPort,
        userRepositoryPort, verificationCodeRepositoryPort);
  }

  @Bean
  public ResendVerificationEmailUseCase resendVerificationEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort,
      ClockPort clockPort) {
    return new ResendVerificationEmailUseCase(
        tenantRepositoryPort,
        clientAppRepositoryPort,
        userRepositoryPort,
        verificationCodeRepositoryPort,
        emailNotificationPort,
        clockPort);
  }

  @Bean
  public CreateAppRoleUseCase createAppRoleUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new CreateAppRoleUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, appRoleRepositoryPort);
  }

  @Bean
  public CreateMembershipUseCase createMembershipUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new CreateMembershipUseCase(
        tenantRepositoryPort, membershipRepositoryPort, appRoleRepositoryPort);
  }

  @Bean
  public ListMembershipsUseCase listMembershipsUseCase(
      MembershipRepositoryPort membershipRepositoryPort) {
    return new ListMembershipsUseCase(membershipRepositoryPort);
  }

  @Bean
  public RevokeMembershipUseCase revokeMembershipUseCase(
      MembershipRepositoryPort membershipRepositoryPort) {
    return new RevokeMembershipUseCase(membershipRepositoryPort);
  }

  @Bean
  public ApproveMembershipUseCase approveMembershipUseCase(
      MembershipRepositoryPort membershipRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    return new ApproveMembershipUseCase(
        membershipRepositoryPort,
        tenantRepositoryPort,
        userRepositoryPort,
        clientAppRepositoryPort,
        emailNotificationPort);
  }

  @Bean
  public ListAppRolesUseCase listAppRolesUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    return new ListAppRolesUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, appRoleRepositoryPort);
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

  // ── T-111: Platform RBAC ─────────────────────────────────────────────────

  @Bean
  public AssignPlatformRoleUseCase assignPlatformRoleUseCase(
      PlatformRoleRepositoryPort platformRoleRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort) {
    return new AssignPlatformRoleUseCase(platformRoleRepositoryPort, platformUserRoleRepositoryPort);
  }

  @Bean
  public RevokePlatformRoleUseCase revokePlatformRoleUseCase(
      PlatformRoleRepositoryPort platformRoleRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort) {
    return new RevokePlatformRoleUseCase(platformRoleRepositoryPort, platformUserRoleRepositoryPort);
  }

  // ── T-111: Tenant RBAC ───────────────────────────────────────────────────

  @Bean
  public CreateTenantRoleUseCase createTenantRoleUseCase(
      TenantRoleRepositoryPort tenantRoleRepositoryPort) {
    return new CreateTenantRoleUseCase(tenantRoleRepositoryPort);
  }

  @Bean
  public AssignTenantRoleUseCase assignTenantRoleUseCase(
      TenantRoleRepositoryPort tenantRoleRepositoryPort,
      TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort) {
    return new AssignTenantRoleUseCase(tenantRoleRepositoryPort, tenantUserRoleRepositoryPort);
  }

  @Bean
  public RevokeTenantRoleUseCase revokeTenantRoleUseCase(
      TenantRoleRepositoryPort tenantRoleRepositoryPort,
      TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort) {
    return new RevokeTenantRoleUseCase(tenantRoleRepositoryPort, tenantUserRoleRepositoryPort);
  }

  @Bean
  public ClockPort clockPort() {
    return new SystemClockProvider();
  }

  @Bean
  public InitiateAuthorizationUseCase initiateAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort, ClientAppRepositoryPort clientAppRepositoryPort) {
    return new InitiateAuthorizationUseCase(tenantRepositoryPort, clientAppRepositoryPort);
  }

  @Bean
  public AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    return new AuthenticateUserForAuthorizationUseCase(
        tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
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
    return new IssueTokensUseCase(
        signingKeyRepositoryPort, tokenSignerPort, tokenClaimsFactoryPort, clockPort);
  }

  @Bean
  public GetJwksUseCase getJwksUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      JwksBuilderPort jwksBuilderPort,
      TenantRepositoryPort tenantRepositoryPort) {
    return new GetJwksUseCase(signingKeyRepositoryPort, jwksBuilderPort, tenantRepositoryPort);
  }

  @Bean
  public GetOidcConfigurationUseCase getOidcConfigurationUseCase(
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}")
          String issuerBaseUrl) {
    return new GetOidcConfigurationUseCase(issuerBaseUrl);
  }

  // ─── Fase 7: Refresh tokens, sesiones, userinfo ───────────────────────────

  @Bean
  public AccessTokenVerifierPort accessTokenVerifierPort() {
    return new RsaJwtTokenVerifier();
  }

  @Bean
  public OpenSessionUseCase openSessionUseCase(SessionRepositoryPort sessionRepositoryPort) {
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
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}")
          String issuerBaseUrl) {
    return new RotateRefreshTokenUseCase(
        refreshTokenRepositoryPort,
        sessionRepositoryPort,
        signingKeyRepositoryPort,
        tokenSignerPort,
        tokenClaimsFactoryPort,
        tenantRepositoryPort,
        clientAppRepositoryPort,
        userRepositoryPort,
        membershipRepositoryPort,
        clockPort,
        issuerBaseUrl);
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
  public ForgotPasswordUseCase forgotPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    return new ForgotPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort,
        verificationCodeRepositoryPort, emailNotificationPort);
  }

  @Bean
  public RecoverPasswordUseCase recoverPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    return new RecoverPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort,
        verificationCodeRepositoryPort, credentialEncoderPort);
  }

  @Bean
  public ResetPasswordUseCase resetPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort) {
    return new ResetPasswordUseCase(
        tenantRepositoryPort,
        userRepositoryPort,
        credentialEncoderPort,
        verificationCodeRepositoryPort);
  }

  @Bean
  public SendPasswordResetCodeUseCase sendPasswordResetCodeUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    return new SendPasswordResetCodeUseCase(
        tenantRepositoryPort,
        userRepositoryPort,
        verificationCodeRepositoryPort,
        emailNotificationPort);
  }

  @Bean
  public ChangePasswordUseCase changePasswordUseCase(
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      AccessTokenVerifierPort accessTokenVerifierPort,
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    return new ChangePasswordUseCase(
        signingKeyRepositoryPort,
        accessTokenVerifierPort,
        tenantRepositoryPort,
        userRepositoryPort,
        credentialEncoderPort);
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
        signingKeyRepositoryPort,
        accessTokenVerifierPort,
        tenantRepositoryPort,
        sessionRepositoryPort,
        refreshTokenRepositoryPort);
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
        signingKeyRepositoryPort,
        accessTokenVerifierPort,
        tenantRepositoryPort,
        membershipRepositoryPort,
        clientAppRepositoryPort);
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
      CredentialEncoderPort credentialEncoderPort,
      SigningKeyRepositoryPort signingKeyRepositoryPort,
      TokenSignerPort tokenSignerPort,
      TokenClaimsFactoryPort tokenClaimsFactoryPort,
      ClockPort clockPort,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}")
          String issuerBaseUrl) {
    return new IssueClientCredentialsTokenUseCase(
        tenantRepositoryPort,
        clientAppRepositoryPort,
        credentialEncoderPort,
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
      ContractEmailVerificationRepositoryPort contractEmailVerificationRepositoryPort,
      AppPlanVersionRepositoryPort versionRepo,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ContractorRepositoryPort contractorRepositoryPort,
      EmailNotificationPort emailNotificationPort,
      KeyGoBillingProperties billingProperties) {
    return new CreateAppContractUseCase(
        contractRepo,
        contractEmailVerificationRepositoryPort,
        versionRepo,
        clientAppRepositoryPort,
        contractorRepositoryPort,
        emailNotificationPort,
        billingProperties.getContractExpiryHours(),
        billingProperties.getVerificationCodeExpiryMinutes());
  }

  @Bean
  public VerifyContractEmailUseCase verifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractEmailVerificationRepositoryPort) {
    return new VerifyContractEmailUseCase(
        contractRepo,
        contractEmailVerificationRepositoryPort);
  }

  @Bean
  public GetAppContractUseCase getAppContractUseCase(AppContractRepositoryPort contractRepo) {
    return new GetAppContractUseCase(contractRepo);
  }

  @Bean
  public MockApprovePaymentUseCase mockApprovePaymentUseCase(
      AppContractRepositoryPort contractRepo,
      PlatformUserRepositoryPort platformUserRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort,
      ContractorRepositoryPort contractorRepositoryPort,
      ContractorUserRepositoryPort contractorUserRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      EmailNotificationPort emailNotificationPort,
      KeyGoBillingProperties billingProperties) {
    return new MockApprovePaymentUseCase(
        contractRepo,
        platformUserRepositoryPort,
        platformUserRoleRepositoryPort,
        contractorRepositoryPort,
        contractorUserRepositoryPort,
        credentialEncoderPort,
        emailNotificationPort,
        billingProperties.isMockPaymentEnabled());
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
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractEmailVerificationRepositoryPort) {
    return new ResumeContractOnboardingUseCase(contractRepo, contractEmailVerificationRepositoryPort);
  }

  @Bean
  public ResendContractVerificationUseCase resendContractVerificationUseCase(
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractEmailVerificationRepositoryPort,
      EmailNotificationPort emailNotificationPort,
      KeyGoBillingProperties billingProperties) {
    return new ResendContractVerificationUseCase(
        contractRepo,
        contractEmailVerificationRepositoryPort,
        emailNotificationPort,
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
    return new CheckAppEntitlementUseCase(
        subscriptionRepo, versionRepo, entitlementRepo, usageRepo);
  }

  // ─── Billing: Plataforma ──────────────────────────────────────────────────

  @Bean
  public GetPlatformPlanCatalogUseCase getPlatformPlanCatalogUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new GetPlatformPlanCatalogUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Bean
  public GetPlatformPlanUseCase getPlatformPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    return new GetPlatformPlanUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Bean
  public GetPlatformSubscriptionUseCase getPlatformSubscriptionUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo) {
    return new GetPlatformSubscriptionUseCase(contractorRepo, subscriptionRepo);
  }

  @Bean
  public CancelPlatformSubscriptionUseCase cancelPlatformSubscriptionUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo) {
    return new CancelPlatformSubscriptionUseCase(contractorRepo, subscriptionRepo);
  }

  @Bean
  public ListPlatformInvoicesUseCase listPlatformInvoicesUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo,
      InvoiceRepositoryPort invoiceRepo) {
    return new ListPlatformInvoicesUseCase(contractorRepo, subscriptionRepo, invoiceRepo);
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

  /**
   * Registers {@link LocaleContextFilter} at {@code HIGHEST_PRECEDENCE + 1} so every request
   * (including those rejected before reaching the DispatcherServlet, e.g. 401 from
   * {@code BootstrapAdminKeyFilter}) carries a properly resolved locale in
   * {@code LocaleContextHolder}.
   *
   * <p>The filter is injected with the same {@link KeyGoLocaleResolver} instance that Spring MVC's
   * DispatcherServlet uses, ensuring consistent locale resolution across all layers.
   */
  @Bean
  public FilterRegistrationBean<LocaleContextFilter> localeContextFilterRegistration(
      KeyGoLocaleResolver keyGoLocaleResolver) {
    FilterRegistrationBean<LocaleContextFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new LocaleContextFilter(keyGoLocaleResolver));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // After RequestTracingFilter
    registration.addUrlPatterns("/*");
    registration.setName("localeContextFilter");
    return registration;
  }

  // ─── Platform user use cases (RFC restructure-multitenant) ─────

  @Bean
  public CreatePlatformUserUseCase createPlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      CredentialEncoderPort credentialEncoder,
      PlatformUserRoleRepositoryPort platformUserRoleRepository) {
    return new CreatePlatformUserUseCase(
        platformUserRepository, credentialEncoder, platformUserRoleRepository);
  }

  @Bean
  public AuthenticatePlatformUserUseCase authenticatePlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      CredentialEncoderPort credentialEncoder) {
    return new AuthenticatePlatformUserUseCase(platformUserRepository, credentialEncoder);
  }

  @Bean
  public SendPlatformPasswordResetCodeUseCase sendPlatformPasswordResetCodeUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    return new SendPlatformPasswordResetCodeUseCase(
        platformUserRepository, codeRepository, emailNotification);
  }

  @Bean
  public ForgotPlatformPasswordUseCase forgotPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    return new ForgotPlatformPasswordUseCase(
        platformUserRepository, codeRepository, emailNotification);
  }

  @Bean
  public CheckPlatformUserEmailUseCase checkPlatformUserEmailUseCase(
      PlatformUserRepositoryPort platformUserRepository) {
    return new CheckPlatformUserEmailUseCase(platformUserRepository);
  }

  @Bean
  public RecoverPlatformPasswordUseCase recoverPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      CredentialEncoderPort credentialEncoder) {
    return new RecoverPlatformPasswordUseCase(
        platformUserRepository, codeRepository, credentialEncoder);
  }

  @Bean
  public ResetPlatformPasswordUseCase resetPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      CredentialEncoderPort credentialEncoder,
      VerificationCodeRepositoryPort codeRepository) {
    return new ResetPlatformPasswordUseCase(
        platformUserRepository, credentialEncoder, codeRepository);
  }

  @Bean
  public ListPlatformUsersUseCase listPlatformUsersUseCase(
      PlatformUserRepositoryPort platformUserRepository) {
    return new ListPlatformUsersUseCase(platformUserRepository);
  }

  @Bean
  public GetPlatformUserUseCase getPlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepository) {
    return new GetPlatformUserUseCase(platformUserRepository);
  }

  @Bean
  public SuspendPlatformUserUseCase suspendPlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepository) {
    return new SuspendPlatformUserUseCase(platformUserRepository);
  }

  @Bean
  public ActivatePlatformUserUseCase activatePlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepository) {
    return new ActivatePlatformUserUseCase(platformUserRepository);
  }

  @Bean
  public IssuePlatformTokensUseCase issuePlatformTokensUseCase(
      PlatformUserRoleRepositoryPort platformUserRoleRepository,
      IssueTokensUseCase issueTokensUseCase,
      SessionRepositoryPort sessionRepository,
      RefreshTokenRepositoryPort refreshTokenRepository,
      ClockPort clock) {
    return new IssuePlatformTokensUseCase(
        platformUserRoleRepository, issueTokensUseCase,
        sessionRepository, refreshTokenRepository, clock);
  }

  @Bean
  public RotatePlatformRefreshTokenUseCase rotatePlatformRefreshTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepository,
      SessionRepositoryPort sessionRepository,
      PlatformUserRoleRepositoryPort platformUserRoleRepository,
      IssueTokensUseCase issueTokensUseCase,
      ClockPort clock,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}")
          String issuerBaseUrl) {
    return new RotatePlatformRefreshTokenUseCase(
        refreshTokenRepository, sessionRepository, platformUserRoleRepository,
        issueTokensUseCase, clock, issuerBaseUrl);
  }

  // ─── Platform Config Port ───────────────────────────────────────────────

  @Bean
  public PlatformConfigPort platformConfigPort(KeyGoPlatformProperties props) {
    return new PlatformConfigPort() {
      @Override
      public java.util.List<String> getAllowedRedirectUris() {
        return props.getAllowedRedirectUris();
      }

      @Override
      public String getApplicationName() {
        return props.getApplicationName();
      }
    };
  }

  // ─── i18n: MessageSource para traducciones ────────────────────────────────

  /**
   * Configures message source for i18n error messages.
   *
   * <p>Loads messages from `classpath:i18n/messages_XX.properties` with hot reload support in
   * development. Cache TTL is 3600 seconds in production to balance between fresh content and
   * performance.
   *
   * <p>Supports: messages.properties (en-US fallback), messages_es.properties,
   * messages_es_CL.properties, messages_en_US.properties, messages_pt_BR.properties,
   * messages_fr.properties.
   */
  @Bean
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:i18n/messages");
    ms.setDefaultEncoding("UTF-8");
    ms.setCacheSeconds(3600); // Cache 1 hour; hot reload disabled in prod
    // Prevent the server's JVM locale from being used as an intermediate fallback step.
    // Without this, locale "en" (no country) would try the JVM system locale (e.g. "es_CL")
    // before falling through to the root messages.properties, producing unexpected behavior.
    ms.setFallbackToSystemLocale(false);
    return ms;
  }

  @Bean
  JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
    return builder ->
        builder
            // Deserializa snake_case (OAuth2) y serializa también en snake_case — globalmente.
            // Los DTOs con @JsonProperty explícito mantienen su nombre definido.
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

            // Robustez ante cambios en payloads (típico en integraciones)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            // Interoperabilidad: acepta variaciones de case además de snake_case
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

            // Payloads limpios
            .changeDefaultPropertyInclusion(
                include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))

            // Coherencia entre ambientes
            .defaultTimeZone(TimeZone.getTimeZone("UTC"));
  }

  // ─── Email Notifications: Thymeleaf + JavaMail ────────────────────────────
  // EmailNotificationAdapter está registered vía @Component o autoconfiguración en keygo-infra
}
