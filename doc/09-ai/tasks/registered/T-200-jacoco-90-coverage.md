# T-200: Coverage JaCoCo 90% por módulo

## Objetivo

Alcanzar **90% de coverage de instrucciones** en todos los módulos de KeyGo Server (excepto keygo-supabase conínimo 85%).

## Estado Actual (2026-04-18)

### Coverage por Módulo

| Módulo | Coverage Actual | Objetivo | Dif |
|--------|---------------|---------|-----|
| keygo-domain | 64% (3505/5466) | 90% | -26% |
| keygo-app | 67% (9498/14081) | 90% | -23% |
| keygo-infra | 67% (932/1371) | 90% | -23% |
| keygo-api | ~70% | 90% | -20% |
| keygo-supabase | 7% (518/6620) | 85% | -78% |

## Catastro: Clases Más Cercanas al 90% (Prioritarias)

Las clases listed requieren menos instrucciones para llegar al 90% de coverage del módulo.

### keygo-infra (67% actual - necesita +138 instr)

| # | Paquete | Clase | % | Missed | Covered | Instrucciones para 90% |
|---|--------|-------|-----|-------|---------|------------------------|
| 1 | io.cmartinezs.keygo.infra.auth.jwt | RsaJwtTokenVerifier | 17% | 28 | 139 | 8 |
| 2 | io.cmartinezs.keygo.infra.adapter.notification | EmailNotificationAdapter | 25% | 372 | 125 | 115 |

### keygo-domain (64% actual - necesita +1,422 instr)

| # | Paquete | Clase | % | Missed | Covered | Instrucciones para 90% |
|---|--------|-------|-----|-------|---------|------------------------|
| 1 | io.cmartinezs.keygo.domain.auth.model | AuthorizationCode | 95% | 10 | 203 | -28 |
| 2 | io.cmartinezs.keygo.domain.auth.model | RefreshToken | 100% | 0 | 167 | 0 |
| 3 | io.cmartinezs.keygo.domain.auth.model | Session | 100% | 0 | 119 | 0 |
| 4 | io.cmartinezs.keygo.domain.auth.model | ScopeSet | 97% | 4 | 154 | -7 |
| 5 | io.cmartinezs.keygo.domain.shared.util | EmailMasker | 99% | 2 | 143 | -33 |
| 6 | io.cmartinezs.keygo.domain.shared.util | SlugUtils | 100% | 0 | 66 | 0 |
| 7 | io.cmartinezs.keygo.domain.tenant.model | Tenant | 75% | 21 | 62 | 15 |
| 8 | io.cmartinezs.keygo.domain.tenant.model | TenantId | 100% | 0 | 43 | 0 |
| 9 | io.cmartinezs.keygo.domain.membership.model | Membership | 68% | 55 | 119 | - |
| 10 | io.cmartinezs.keygo.domain.membership.model | TenantRole | 73% | 25 | 69 | - |
| 11 | io.cmartinezs.keygo.domain.clientapp.model | ClientApp | 81% | 45 | 198 | - |
| 12 | io.cmartinezs.keygo.domain.user.model | User | 93% | 14 | 186 | - |

### keygo-app (67% actual - necesita +3,237 instr)

| # | Paquete | Clase | % | Missed | Covered | Instrucciones para 90% |
|---|--------|-------|-----|-------|---------|------------------------|
| 1 | io.cmartinezs.keygo.app.shared | PageFilter | 100% | 0 | 73 | 0 |
| 2 | io.cmartinezs.keygo.app.shared | PagedResult | 96% | 3 | 64 | -8 |
| 3 | io.cmartinezs.keygo.app.platform.usecase | GetPlatformStatsUseCase | 100% | 0 | 55 | 0 |
| 4 | io.cmartinezs.keygo.app.platform.usecase | GetServiceInfoUseCase | 100% | 0 | 9 | 0 |
| 5 | io.cmartinezs.keygo.app.tenant.usecase | CreateTenantUseCase | 100% | 0 | 36 | 0 |
| 6 | io.cmartinezs.keygo.app.tenant.usecase | ListTenantsUseCase | 100% | 0 | 11 | 0 |
| 7 | io.cmartinezs.keygo.app.user.usecase | CreatePlatformUserUseCase | 100% | 0 | 95 | 0 |
| 8 | io.cmartinezs.keygo.app.clientapp.usecase | ListClientAppsUseCase | 100% | 0 | 31 | 0 |
| 9 | io.cmartinezs.keygo.app.clientapp.usecase | GetClientAppUseCase | 100% | 0 | 41 | 0 |
| 10 | io.cmartinezs.keygo.app.membership.usecase | RemoveRoleParentUseCase | 100% | 0 | 52 | 0 |
| 11 | io.cmartinezs.keygo.app.membership.usecase | RevokePlatformRoleUseCase | 100% | 0 | 25 | 0 |

### keygo-api (~70% actual)

| # | Paquete | Clase | % | Missed | Covered |
|---|--------|-------|-----|-------|---------|
| 1 | io.cmartinezs.keygo.api.shared | ResponseCode | 100% | 0 | 1091 |
| 2 | io.cmartinezs.keygo.api.platform.controller | PlatformRoleController | 100% | 0 | 29 |
| 3 | io.cmartinezs.keygo.api.platform.controller | ServiceInfoController | 100% | 0 | 41 |
| 4 | io.cmartinezs.keygo.api.platform.controller | PlatformStatsController | 100% | 0 | 68 |
| 5 | io.cmartinezs.keygo.api.discovery.controller | PublicDiscoveryController | 100% | 0 | 181 |

## Plan de Implementación

### Fase 1: Completar clasesnear al 90% (keygo-infra) ✅ COMPLETADO
- [x] SendEmailCommand - 100%
- [x] PkceVerifier - 86%
- [x] RsaJwtTokenVerifier - 83%
- [x] EmailNotificationAdapter - 25% (bloqueante)

### Fase 2: Módulos principales
- [ ] keygo-domain: agregar tests por paquete
- [ ] keygo-app: agregar tests por use case
- [ ] keygo-api: agregar tests por controller

### Fase 3: keygo-supabase
- [ ]threshold mínimo 85% (mucho código JPA/repository)
- requiere ~5,600 instrucciones adicionales

## Tests Creados

### keygo-infra
- `SendEmailCommandTest.java` - 6 tests
- `PkceVerifierTest.java` - 11 tests
- `RsaJwtTokenVerifierTest.java` - 2 tests
- `RsaJwtTokenVerifierFlowTest.java` - 3 tests
- `EmailNotificationAdapterLinksTest.java` - 3 tests

## Métricas Finales Objetivo

| Módulo | Instrucciones Totales | 90% Covered | Min |
|--------|---------------------|-------------|-----|
| keygo-domain | 5,466 | 4,919 | 4,646 |
| keygo-app | 14,081 | 12,673 | 11,969 |
| keygo-infra | 1,371 | 1,234 | 1,165 |
| keygo-api | ~10,000 | 9,000 | 8,500 |
| keygo-supabase | 6,620 | - | 5,627 (85%) |

## Estado
- 🔲 EN PROGRESO: Catastro creado
- 🔲 PENDIENTE: Configurar mínimos JaCoCo
- 🔲 PENDIENTE: Agregar tests priorizados
- 🔲 PENDIENTE: Verificar coverage 90%

## Anexo

Reporte detallado de coverage por clase: [T-200-jacoco-90-coverage-anexo.md](T-200-jacoco-90-coverage-anexo.md)

---
*Creado: 2026-04-18*
*Actualizado: 2026-04-18*