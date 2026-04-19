# Anexo: Cobertura JaCoCo por Clase (2026-04-18)

> **Nota:** Este anexo lista las clases ordenadas por coverage. La mayora de las clases 80-99% ya tienen tests pero-coverage parcial. Completar al 100% requiere expandir los tests - existentes.

Reporte detallado de coverage de instrucciones por cada clase en los módulos de KeyGo Server.

## Resumen Ejecutivo

| Módulo | Coverage | Objetivo | Diferencia |
|--------|---------|---------|----------|
| keygo-infra | 68% | 90% | -22% |
| keygo-domain | 66% | 90% | -24% |
| keygo-app | 67% | 90% | -23% |
| keygo-api | ~70% | 90% | -20% |
| keygo-supabase | 7% | 85% | -78% |

---

## keygo-infra (68% - 941/1371 instrucciones cubiertas)

### Paquete: io.cmartinezs.keygo.infra.mail

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| SendEmailCommand | 100% | 24 | 24 |
| ConfigurableEmailStrategy | 100% | 189 | 189 |
| EmailStrategy | 75% | 6 | 9 |

### Paquete: io.cmartinezs.keygo.infra.config

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| KeyGoUiProperties.UiPath | 100% | 6 | 6 |

### Paquete: io.cmartinezs.keygo.infra.adapter.notification

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| EmailNotificationAdapter | 25% | 125 | 497 |

### Paquete: io.cmartinezs.keygo.infra.auth.security

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| PkceVerifier | 86% | 44 | 51 |

### Paquete: io.cmartinezs.keygo.infra.auth.jwks

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| JwkSetBuilder | 94% | 75 | 80 |

### Paquete: io.cmartinezs.keygo.infra.auth.jwt

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| StandardTokenClaimsFactory | 96% | 170 | 177 |
| RsaJwtTokenSigner | 90% | 154 | 171 |
| RsaJwtTokenVerifier | 88% | 148 | 167 |

---

## keygo-domain (66% - 3624/5466 instrucciones cubiertas)

### Paquete: io.cmartinezs.keygo.domain.auth.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| RefreshToken | 100% | 167 | 167 |
| Session | 100% | 119 | 119 |
| SigningKey | 100% | 19 | 19 |
| SigningKeyStatus | 100% | 21 | 21 |
| AuthorizationCodeId | 100% | 27 | 27 |
| RefreshTokenId | 100% | 27 | 27 |
| SigningKeyId | 100% | 16 | 16 |
| SigningKeyAlgorithm | 100% | 21 | 21 |
| ScopeSet | 97% | 154 | 158 |
| AuthorizationCode | 95% | 203 | 213 |
| CodeChallenge | 96% | 91 | 95 |
| SessionId | 85% | 23 | 27 |
| AuthorizationCodeStatus | 55% | 39 | 71 |
| RefreshTokenStatus | 55% | 39 | 71 |
| SessionStatus | 50% | 32 | 64 |

### Paquete: io.cmartinezs.keygo.domain.shared.util

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| SlugUtils | 100% | 66 | 66 |
| EmailMasker | 99% | 143 | 145 |

### Paquete: io.cmartinezs.keygo.domain.shared.exception

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| ExceptionLayer | 100% | 27 | 27 |
| DomainException | 46% | 5 | 11 |
| KeyGoException | 39% | 7 | 18 |

### Paquete: io.cmartinezs.keygo.domain.tenant.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| TenantId | 100% | 43 | 43 |
| TenantStatus | 100% | 27 | 27 |
| TenantSlug | 91% | 59 | 65 |
| Tenant | 75% | 62 | 83 |

### Paquete: io.cmartinezs.keygo.domain.clientapp.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| ClientAppId | 100% | 43 | 43 |
| ClientType | 100% | 15 | 15 |
| AllowedGrant | 100% | 27 | 27 |
| AllowedScope | 100% | 24 | 24 |
| ClientAppStatus | 100% | 21 | 21 |
| AccessPolicy | 100% | 42 | 42 |
| RegistrationPolicy | 100% | 27 | 27 |
| RedirectUri | 93% | 38 | 41 |
| ClientId | 91% | 30 | 33 |
| ClientApp | 81% | 198 | 243 |

### Paquete: io.cmartinezs.keygo.domain.membership.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| PlatformRole | 100% | 63 | 63 |
| MembershipStatus | 100% | 21 | 21 |
| MembershipRole | 100% | 23 | 23 |
| AppRole | 84% | 42 | 50 |
| AppRoleId | 86% | 36 | 42 |
| TenantRole | 73% | 69 | 94 |
| TenantUserRole | 66% | 33 | 50 |
| RoleCode | 85% | 50 | 59 |
| Membership | 68% | 119 | 174 |
| MembershipId | 74% | 31 | 42 |
| TenantRoleId | 43% | 18 | 42 |
| PlatformUserRole | 0% | 0 | 72 |
| PlatformRoleCode | 0% | 0 | 44 |
| PlatformUserRoleId | 0% | 0 | 42 |
| UserAccessEntry | 0% | 0 | 18 |

### Paquete: io.cmartinezs.keygo.domain.user.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| UserId | 100% | 43 | 43 |
| UserStatus | 100% | 39 | 39 |
| Username | 100% | 33 | 33 |
| VerificationPurpose | 100% | 21 | 21 |
| PasswordHash | 100% | 23 | 23 |
| EmailAddress | 100% | 34 | 34 |
| VerificationCode | 100% | 102 | 102 |
| User | 93% | 186 | 200 |
| PlatformUser | 0% | 0 | 157 |
| NotificationPreferences | 0% | 0 | 83 |
| PasswordPolicy | 0% | 0 | 4 |
| PasswordValidationHelper | 0% | 0 | 74 |

### Paquete: io.cmartinezs.keygo.domain.billing.contracting.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| ContractStatus | 100% | 63 | 63 |
| AppContract | 77% | 229 | 297 |
| ContractEmailVerification | 0% | 0 | 111 |

### Paquete: io.cmartinezs.keygo.domain.billing.contractor.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| ContractorType | 100% | 15 | 15 |
| ContractorStatus | 100% | 27 | 27 |
| Contractor | 100% | 77 | 77 |
| ContractorUser | 0% | 0 | 36 |
| ContractorUserRole | 0% | 0 | 21 |

### Paquete: io.cmartinezs.keygo.domain.billing.catalog.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| AppPlanStatus | 100% | 15 | 15 |
| EnforcementMode | 100% | 15 | 15 |
| AppPlan | 84% | 53 | 63 |

### Paquete: io.cmartinezs.keygo.domain.billing.subscription.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| AppSubscription | 0% | 0 | 112 |
| SubscriptionStatus | 0% | 0 | 39 |
| SubscriberType | 0% | 0 | 21 |

### Paquete: io.cmartinezs.keygo.domain.billing.invoice.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| Invoice | 0% | 0 | 5 |
| InvoiceStatus | 0% | 0 | 33 |

### Paquete: io.cmartinezs.keygo.domain.billing.payment.model

| Clase | % | Instrucciones Cubiertas | Instrucciones Totales |
|-------|---|---------------------|-------------------|
| PaymentStatus | 0% | 0 | 33 |

---

## keygo-app (67% - 9498/14081 instrucciones cubiertas)

### Paquetes con mayor coverage (>90%)

| Paquete | Clase | % | Cubiertas | Totales |
|--------|-------|-------|---------------------|-------------------|
| io.cmartinezs.keygo.app.shared | PageFilter | 100% | 73 | 73 |
| io.cmartinezs.keygo.app.platform.usecase | GetPlatformStatsUseCase | 100% | 55 | 55 |
| io.cmartinezs.keygo.app.platform.usecase | GetServiceInfoUseCase | 100% | 9 | 9 |
| io.cmartinezs.keygo.app.platform.usecase | GetPlatformDashboardUseCase | 100% | 633 | 633 |
| io.cmartinezs.keygo.app.tenant.usecase | CreateTenantUseCase | 100% | 36 | 36 |
| io.cmartinezs.keygo.app.tenant.usecase | ListTenantsUseCase | 100% | 11 | 11 |
| io.cmartinezs.keygo.app.billing.platform.usecase | CancelPlatformSubscriptionUseCase | 100% | 59 | 59 |
| io.cmartinezs.keygo.app.billing.platform.usecase | GetPlatformPlanCatalogUseCase | 100% | 67 | 69 |
| io.cmartinezs.keygo.app.billing.platform.usecase | GetPlatformPlanUseCase | 97% | 69 | 71 |
| io.cmartinezs.keygo.app.billing.platform.usecase | ListPlatformInvoicesUseCase | 100% | 50 | 50 |
| io.cmartinezs.keygo.app.billing.platform.usecase | GetPlatformSubscriptionUseCase | 100% | 41 | 41 |
| io.cmartinezs.keygo.app.clientapp.usecase | ListClientAppsUseCase | 100% | 31 | 31 |
| io.cmartinezs.keygo.app.clientapp.usecase | GetClientAppUseCase | 100% | 41 | 41 |
| io.cmartinezs.keygo.app.membership.usecase | CreateTenantRoleUseCase | 100% | 48 | 48 |
| io.cmartinezs.keygo.app.membership.usecase | RevokePlatformRoleUseCase | 100% | 25 | 25 |
| io.cmartinezs.keygo.app.membership.usecase | RemoveRoleParentUseCase | 100% | 52 | 52 |
| io.cmartinezs.keygo.app.membership.usecase | AssignPlatformRoleUseCase | 100% | 65 | 65 |
| io.cmartinezs.keygo.app.membership.usecase | AssignRoleParentUseCase | 100% | 122 | 122 |
| io.cmartinezs.keygo.app.user.usecase | CreatePlatformUserUseCase | 100% | 95 | 95 |
| io.cmartinezs.keygo.app.user.usecase | CreateUserUseCase | 100% | 110 | 110 |
| io.cmartinezs.keygo.app.user.usecase | GetUserUseCase | 100% | 43 | 43 |
| io.cmartinezs.keygo.app.user.usecase | ActivateUserUseCase | 100% | 49 | 49 |
| io.cmartinezs.keygo.app.user.usecase | UpdateUserUseCase | 100% | 70 | 70 |
| io.cmartinezs.keygo.app.user.usecase | SuspendUserUseCase | 100% | 49 | 49 |

### Paquetes con coverage bajo (<30%)

| Paquete | Clase | % | Cubiertas | Totales |
|--------|-------|-------|---------------------|-------------------|
| io.cmartinezs.keygo.app.platform.usecase | RotatePlatformRefreshTokenUseCase | 0% | 0 | 273 |
| io.cmartinezs.keygo.app.auth.usecase | ExchangeAuthorizationCodeUseCase | 0% | 0 | 221 |
| io.cmartinezs.keygo.app.auth.usecase | IssueClientCredentialsTokenUseCase | 0% | 0 | 238 |
| io.cmartinezs.keygo.app.auth.usecase | AuthenticateUserForAuthorizationUseCase | 0% | 0 | 108 |
| io.cmartinezs.keygo.app.auth.usecase | GetUserInfoUseCase | 0% | 0 | 173 |
| io.cmartinezs.keygo.app.auth.usecase | InitiateAuthorizationUseCase | 0% | 0 | 86 |
| io.cmartinezs.keygo.app.auth.usecase | TerminateSessionUseCase | 0% | 0 | 40 |
| io.cmartinezs.keygo.app.billing.contracting.usecase | ResendContractVerificationUseCase | 0% | 0 | 166 |
| io.cmartinezs.keygo.app.billing.contracting.usecase | ActivateAppContractUseCase | 0% | 0 | 306 |
| io.cmartinezs.keygo.app.billing.contracting.usecase | GetAppContractUseCase | 0% | 0 | 26 |
| io.cmartinezs.keygo.app.billing.contracting.usecase | ResumeContractOnboardingUseCase | 0% | 0 | 77 |
| io.cmartinezs.keygo.app.billing.invoice.usecase | ListAppInvoicesUseCase | 0% | 0 | 11 |
| io.cmartinezs.keygo.app.billing.catalog.usecase | GetAppPlanUseCase | 0% | 0 | 86 |
| io.cmartinezs.keygo.app.billing.subscription.usecase | CancelAppSubscriptionUseCase | 0% | 0 | 45 |
| io.cmartinezs.keygo.app.billing.subscription.usecase | GetAppSubscriptionUseCase | 0% | 0 | 29 |
| io.cmartinezs.keygo.app.user.usecase | GetPlatformUserProfileUseCase | 0% | 0 | 85 |
| io.cmartinezs.keygo.app.user.usecase | SendPlatformPasswordResetCodeUseCase | 0% | 0 | 144 |
| io.cmartinezs.keygo.app.user.usecase | UpdatePlatformUserProfileUseCase | 0% | 0 | 104 |
| io.cmartinezs.keygo.app.user.usecase | ActivatePlatformUserUseCase | 0% | 0 | 30 |
| io.cmartinezs.keygo.app.user.usecase | SuspendPlatformUserUseCase | 0% | 0 | 30 |
| io.cmartinezs.keygo.app.user.usecase | GetRegistrationInfoUseCase | 0% | 0 | 89 |
| io.cmartinezs.keygo.app.user.usecase | GetPlatformRegistrationInfoUseCase | 0% | 0 | 90 |
| io.cmartinezs.keygo.app.user.orchestrator | SelfRegistrationOrchestrator | 0% | 0 | 105 |
| io.cmartinezs.keygo.app.membership.usecase | AssignTenantRoleUseCase | 0% | 0 | 110 |
| io.cmartinezs.keygo.app.membership.usecase | RevokeTenantRoleUseCase | 0% | 0 | 43 |
| io.cmartinezs.keygo.app.role.filter | AppRoleFilter | 0% | 0 | 42 |
| io.cmartinezs.keygo.app.membership.filter | MembershipFilter | 0% | 0 | 48 |
| io.cmartinezs.keygo.app.tenant.context | TenantContextHolder | 0% | 0 | 23 |

---

## keygo-api (~70%)

### Clases con coverage alto (>90%)

| Paquete | Clase | % |
|--------|-------|-----|
| io.cmartinezs.keygo.api.shared | ResponseCode | 100% |
| io.cmartinezs.keygo.api.platform.controller | PlatformRoleController | 100% |
| io.cmartinezs.keygo.api.platform.controller | ServiceInfoController | 100% |
| io.cmartinezs.keygo.api.platform.controller | PlatformStatsController | 100% |
| io.cmartinezs.keygo.api.discovery.controller | PublicDiscoveryController | 100% |
| io.cmartinezs.keygo.api.platform.controller | PlatformOidcMetadataController | 91% |

### Clases con coverage bajo (<30%)

| Paquete | Clase | % |
|--------|-------|-----|
| io.cmartinezs.keygo.api.auth.controller | AuthorizationController | 18% |
| io.cmartinezs.keygo.api.platform.controller | PlatformAccountController | 0% |
| io.cmartinezs.keygo.api.user.controller | AccountProfileController | 0% |
| io.cmartinezs.keygo.api.membership.controller | TenantMembershipController | 0% |
| io.cmartinezs.keygo.api.registration.controller | RegistrationController | 0% |
| io.cmartinezs.keygo.api.user.util | UserAgentParser | 0% |
| io.cmartinezs.keygo.api.auth.session | AuthorizationSessionState | 0% |

---

## keygo-supabase (7% - 518/6620)

Este módulo tiene coverage muy bajo por la naturaleza del código (JPA entities y repositorios). Las clases con mayor impacto serían:

### Clases con mayor coverage existente (>50%)

| Paquete | Clase | % |
|--------|-------|-----|
| io.cmartinezs.keygo.supabase.tenant.mapper | TenantPersistenceMapper | 92% |
| io.cmartinezs.keygo.supabase.clientapp.mapper | ClientAppPersistenceMapper | 92% |
| io.cmartinezs.keygo.supabase.auth.mapper | SigningKeyPersistenceMapper | 75% |
| io.cmartinezs.keygo.supabase.user.mapper | UserPersistenceMapper | 64% |

### Clases con coverage 0%

Casi todas las clases de este módulo tienen coverage bajo 30% debido a la complejidad del mocking de JPA.

---

## Recomendaciones para aumentar coverage

### Prioridad 1: Clases pequeñas con coverage 0% en keygo-domain

1. **PlatformUser** (157 instr) - modelo central de usuario
2. **NotificationPreferences** (83 instr) - modelo de preferencias
3. **PasswordValidationHelper** (74 instr) - utilitario de validación
4. **ContractEmailVerification** (111 inp) - modelo de verificación
5. **AppSubscription** (112 inp) - modelo de suscripción

### Prioridad 2: Use cases críticos en keygo-app

1. **RotatePlatformRefreshTokenUseCase** (273 inp)
2. **ExchangeAuthorizationCodeUseCase** (221 inp)
3. **IssueClientCredentialsTokenUseCase** (238 inp)

### Prioridad 3: Controllers keygo-api

1. **AuthorizationController** (150 inp cubiertas de 823)
2. **PlatformAccountController** (0%)
3. **UserAgentParser** (0%)

---

*Documento generado: 2026-04-18*
*Fuente: Informes JaCoCo CSV por módulo*