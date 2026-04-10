# Propuesta de Solución y Roadmap Consolidado — KeyGo Server

> **Descripción:** Descomposición del roadmap en fases estratégicas, hitos claros, dependencias explícitas y priorización.

**Fecha:** 2026-04-05  
**Horizonte:** 2026-04-05 a 2026-10-31 (6 meses)

---

## 1. Visión Estratégica

### **Misión**
KeyGo Server es la **plataforma de autenticación y control de acceso multi-tenant para ecosistemas SaaS**, con OAuth2/OIDC completo, RBAC jerárquico y facturación integrada.

### **Objetivos por Horizonte**

| Horizonte | Objetivo | Impacto | Status |
|---|---|---|---|
| **Corto plazo (4 sem)** | Solidificar hosted login seguro; documentación centralizada; deuda técnica menor | MVP lanzable; confianza en arquitectura | 🟡 En ejecución |
| **Mediano plazo (8 sem)** | Multi-dominio hardening (auth → payment), gateway real, renewal automática | Monetización posible; expansión mercado | 🔲 No iniciado |
| **Largo plazo (16 sem)** | Observabilidad avanzada, SCIM, federated session, multi-moneda | Scale enterprise; integración ecosistema | 🔲 No iniciado |

---

## 2. Hitos y Fases (Roadmap Consolidado)

### **HITO 1: Hosted Login Seguro & Documentación (P0) — 4 semanas**
**Período:** 2026-04-05 a 2026-05-03  
**Objetivo:** MVP lanzable con documentación de referencia única

#### **Propuestas Incluidas**
| ID | Propuesta | Status | Effort | Bloqueador |
|---|---|---|---|---|
| **T-030** | Verificación referencias Markdown rotas post-reorganización | 🔲 Pendiente | 4 h | Ninguno |
| **T-031** | Automatizar verificación links rotos en CI (lychee/markdown-link-check) | 🔲 Pendiente | 6 h | T-030 |
| **T-023** | Configurar lint/formato (Checkstyle/Spotless) | 🔲 Pendiente | 8 h | Ninguno |
| **V-067** | Eliminar `SigningKeyBootstrapService` redundante | 🔲 Pendiente | 2 h | Ninguno |
| **T-051** | Suite autorización por endpoint (@PreAuthorize matriz) | 🔲 Pendiente | 12 h | Ninguno |
| **T-035** | Detección replay attack: USED token → revocar cadena | 🔲 Pendiente | 6 h | Ninguno |
| **T-062** | Handler específico `MissingServletRequestParameterException` → 400 | 🔲 Pendiente | 4 h | Ninguno |
| **T-036** | TTL configurable refresh tokens (`application.yml`) | 🔲 Pendiente | 3 h | Ninguno |
| **T-043** | Scope filtering en `/userinfo` (profile, email, phone) | 🔲 Pendiente | 6 h | Ninguno |

**Subtotal:** 9 propuestas, 51 horas (~6 dev-days)

#### **Deliverables**
- ✅ `docs/product-design/` centraliza todo (SITUACION_ACTUAL, ANALISIS_DOLORES, REQUERIMIENTOS)
- ✅ Markdown references validadas + CI enforcement (T-030/T-031)
- ✅ Lint/formato automático (T-023)
- ✅ Redundancia signing key eliminada (V-067)
- ✅ Matriz @PreAuthorize exhaustiva (T-051)
- ✅ Replay attack detection (T-035)

---

### **HITO 2: Pré-Monetización & Stability (P1) — 4 semanas**
**Período:** 2026-05-04 a 2026-06-01  
**Objetivo:** Billing operacional, tests integración, caché performance

#### **Propuestas Incluidas**
| ID | Propuesta | Status | Effort | Bloqueador |
|---|---|---|---|---|
| **T-091** | Test integración Testcontainers: JPA ↔ Flyway coherencia | 🔲 Pendiente | 12 h | Ninguno |
| **T-013** | Tests integración Testcontainers para `keygo-supabase` | 🔲 Pendiente | 16 h | T-091 |
| **T-025** | Tests integración flujo completo Tenant | 🔲 Pendiente | 10 h | T-091 |
| **T-074** | Caché `@Cacheable` en GetPlatformDashboardUseCase (TTL 60s) | 🔲 Pendiente | 6 h | Ninguno |
| **T-075** | `GET /api/v1/admin/tenants/{slug}/dashboard` (ADMIN_TENANT) | 🔲 Pendiente | 8 h | T-074 |
| **T-070** | `GET /api/v1/tenants/{slug}/stats` con filtros fecha | 🔲 Pendiente | 8 h | Ninguno |
| **T-053** | Script SQL verificación post-seed V14 (conteos esperados) | 🔲 Pendiente | 4 h | Ninguno |
| **T-083** | Endpoint `GET /billing/invoices/{invoiceId}` | 🔲 Pendiente | 4 h | Ninguno |
| **T-094** | Test unitario `AppPlanBillingOptionRepositoryAdapter` | 🔲 Pendiente | 6 h | Ninguno |
| **T-095** | Validar `CreateAppPlanCommand`: ≥1 billingOption con `isDefault=true` | 🔲 Pendiente | 3 h | Ninguno |
| **T-096** | Agregar `@NotNull`, `@Valid` en `CreateAppPlanRequest` + tests | 🔲 Pendiente | 4 h | Ninguno |

**Subtotal:** 11 propuestas, 81 horas (~10 dev-days)

#### **Deliverables**
- ✅ Suite tests integración (JPA ↔ migraciones, flujos tenant)
- ✅ Dashboard platform + tenant cacheado
- ✅ Billing operacional (endpoint facturas, validaciones completas)
- ✅ Cobertura tests aumenta (~50% → 65%)

---

### **HITO 3: Gateway Real & Renovación (P1) — 6 semanas**
**Período:** 2026-06-02 a 2026-07-14  
**Objetivo:** Monetización posible; suscripciones automáticas

#### **Propuestas Incluidas**
| ID | Propuesta | Status | Effort | Bloqueador |
|---|---|---|---|---|
| **T-084** | Integración gateway Stripe/MercadoPago (reemplaza mock) | 🔲 Pendiente | 24 h | Ninguno |
| **T-085** | Renovación automática @Scheduled job | 🔲 Pendiente | 12 h | T-084 |
| **T-090** | Motor dunning: reintentos D+1/D+3/D+7 + email | 🔲 Pendiente | 16 h | T-084, T-085 |
| **T-086** | Soporte Bearer TENANT_USER en `/billing/subscription` | 🔲 Pendiente | 6 h | Ninguno |
| **T-098** | Filtro `?subscriberType` en catalog | 🔲 Pendiente | 4 h | Ninguno |
| **T-099** | Caché `@Cacheable` +Caffeine en GetAppPlanCatalogUseCase (TTL 5m) | 🔲 Pendiente | 4 h | Ninguno |
| **T-097** | `PUT /billing/plans/{planCode}/billing-options` sin crear versión | 🔲 Pendiente | 8 h | Ninguno |
| **T-115** | Aumentar JaCoCo en `keygo-supabase`: 0.15 → 0.60 | 🔲 Pendiente | 20 h | Ninguno |

**Subtotal:** 8 propuestas, 94 horas (~12 dev-days)

#### **Deliverables**
- ✅ Gateway real integrado (Stripe/MercadoPago)
- ✅ Renovación automática suscripciones
- ✅ Dunning (reintentos fallidos)
- ✅ Cobertura tests `keygo-supabase` 60%+

---

### **HITO 4: Multi-Dominio & Hardening (P1) — 6 semanas**
**Período:** 2026-07-15 a 2026-08-26  
**Objetivo:** Contrato multi-dominio, federación de sesión, observabilidad

#### **Propuestas Incluidas**
| ID | Propuesta | Status | Effort | Bloqueador |
|---|---|---|---|---|
| **T-057** | Contrato formal multi-dominio (validación, firma, CORS, cookies) | 🔲 Pendiente | 12 h | Ninguno |
| **T-058** | Patrón BFF para canje authorization_code (backend, SPA segura) | 🔲 Pendiente | 16 h | Ninguno |
| **T-063** | Incorporar `traceId/requestId` en `ErrorData` | 🔲 Pendiente | 6 h | Ninguno |
| **T-066** | Agregar `endpointHint/actionHint` para errores CLIENT_TECHNICAL | 🔲 Pendiente | 4 h | Ninguno |
| **T-064** | Catálogo i18n avanzado: origin + clientRequestCause + locale | 🔲 Pendiente | 10 h | Ninguno |
| **T-108** | Geolocalización IP en sesiones (GeoIpPort + adapter) | 🔲 Pendiente | 10 h | Ninguno |
| **T-109** | Job cleanup sesiones expiradas/terminadas (@Scheduled) | 🔲 Pendiente | 8 h | Ninguno |
| **T-073** | Micrometer + Prometheus: métricas keygo_tenants_total, etc. | 🔲 Pendiente | 14 h | Ninguno |
| **T-076** | Tabla audit_events (V24) + endpoint `GET /admin/audit` | 🔲 Pendiente | 16 h | Ninguno |

**Subtotal:** 9 propuestas, 96 horas (~12 dev-days)

#### **Deliverables**
- ✅ Contrato multi-dominio documentado y implementado
- ✅ Patrón BFF para seguridad SPA
- ✅ Trazabilidad completa (traceId, requestId, audit trail)
- ✅ Observabilidad: Prometheus + Grafana
- ✅ Sesiones con geolocalización

---

### **HITO 5: Largo Plazo — Post-Monetización (P2/P3) — 16+ semanas**

#### **Track A: Integraciones Enterprise**
| ID | Propuesta | Status | Effort | Horizonte |
|---|---|---|---|---|
| **T-047** | SCIM 2.0 endpoint aprovisionamiento | 🔲 Pendiente | 20 h | Q3 2026 |
| **T-048** | Custom attributes schema por tenant | 🔲 Pendiente | 12 h | Q3 2026 |
| **T-044** | Membership attributes metadata | 🔲 Pendiente | 8 h | Q2 2026 |
| **T-045** | Claim mappers por ClientApp | 🔲 Pendiente | 10 h | Q3 2026 |
| **T-055** | Bootstrap programático tenants/apps/roles (control-plane) | 🔲 Pendiente | 14 h | Q3 2026 |

#### **Track B: Expansión Global (Multi-Moneda)**
| ID | Propuesta | Status | Effort | Horizonte |
|---|---|---|---|---|
| **T-089** | Multi-moneda: tabla exchange_rates | 🔲 Pendiente | 12 h | Q3 2026 |
| **T-100** | Pricing tiers/escalonado | 🔲 Pendiente | 10 h | Q3 2026 |
| **T-101** | Overrides precio por moneda | 🔲 Pendiente | 8 h | Q3 2026 |
| **T-102** | Precios dinámicos vía webhook | 🔲 Pendiente | 12 h | Q4 2026 |
| **T-088** | CFDI México (factura electrónica) | 🔲 Pendiente | 16 h | Q4 2026 |
| **T-087** | PDF de facturas (iText/JasperReports) | 🔲 Pendiente | 12 h | Q3 2026 |

#### **Track C: Seguridad Avanzada**
| ID | Propuesta | Status | Effort | Horizonte |
|---|---|---|---|---|
| **T-028** | KMS externo (AWS KMS / HashiCorp Vault) | 🔲 Pendiente | 18 h | Q4 2026 |
| **T-038** | Lista negra JTI revocación inmediata (Redis) | 🔲 Pendiente | 10 h | Q3 2026 |
| **T-020** | OpenTelemetry avanzada + tracing distribuido | 🔲 Pendiente | 16 h | Q4 2026 |
| **T-059** | Redirect OAuth2 clásico (HTTP 302) | 🔲 Pendiente | 10 h | Q3 2026 |

#### **Track D: Futuras Épicas**
| ID | Propuesta | Status | Effort | Horizonte |
|---|---|---|---|---|
| **F-041** | SSO multi-app (sesión compartida ecosistema) | 🔲 Pendiente | 32 h | Q4 2026+ |
| **F-042** | Account connections (apps externas vinculadas) | 🔲 Pendiente | 16 h | Q3 2026 |
| **F-040** | RBAC granular por permiso/acción | 🔲 Pendiente | 20 h | Q4 2026 |
| **T-032** | Generador site estático (MkDocs/Docusaurus) | 🔲 Pendiente | 12 h | Q4 2026 |

---

## 3. Mapa de Dependencias (Critical Path)

```
HITO 1 (P0)
├── V-067 (redundancia signing key) [2h]
├── T-023 (lint/formato) [8h]
├── T-030 (refs markdown) [4h] ┐
│   └── T-031 (CI links) [6h] ┘
├── T-051 (matriz @PreAuthorize) [12h]
├── T-035 (replay attack) [6h]
├── T-036 (TTL configurable) [3h]
├── T-043 (scope filtering) [6h]
├── T-062 (MissingServletRequestParameterException) [4h]
└── Docs product-design completas

        ↓ (Hito 1 completo)

HITO 2 (P1)
├── T-091 (test integración Testcontainers) [12h]
│   ├── T-013 (keygo-supabase integration tests) [16h] ┐
│   └── T-025 (tenant flow integration tests) [10h] ┤
├── T-074 (caché dashboard) [6h] ┐
│   └── T-075 (tenant dashboard) [8h] ┘
├── T-070 (tenant stats con filtros) [8h]
├── T-053 (script verificación seed) [4h]
├── Billing: T-083, T-094, T-095, T-096 (18h)
└── Cobertura tests 65%+

        ↓ (Hito 2 completo)

HITO 3 (P1)
├── T-084 (gateway real) [24h]
│   ├── T-085 (renovación automática) [12h] ┐
│   └── T-090 (dunning) [16h] ┘
├── T-086 (Bearer TENANT_USER billing) [6h]
├── T-098, T-099, T-097 (billing optimizaciones) [16h]
└── T-115 (cobertura keygo-supabase 60%+) [20h]

        ↓ (Hito 3 completo)

HITO 4 (P1)
├── T-057 (contrato multi-dominio) [12h]
├── T-058 (patrón BFF) [16h]
├── T-063 (traceId/requestId) [6h]
├── T-066 (endpointHint/actionHint) [4h]
├── T-064 (i18n avanzado) [10h]
├── T-108 (geolocalización) [10h]
├── T-109 (cleanup sessions) [8h]
├── T-073 (Prometheus/Micrometer) [14h]
└── T-076 (audit trail) [16h]

        ↓ (Hito 4 completo → LANZAMIENTO POSIBLE)

HITO 5 (P2/P3) — Post-lanzamiento
├── [Track A] SCIM + custom attributes (60h)
├── [Track B] Multi-moneda + CFDI (70h)
├── [Track C] KMS, SSL JTI, OpenTelemetry (54h)
└── [Track D] Épicas futuras (80h+)
```

---

## 4. Priorización: P0 → P3

| Prioridad | Hitos | Justificación | Impacto |
|---|---|---|---|
| **P0** | HITO 1 | Documentación + deuda técnica + seguridad | MVP lanzable sin deuda |
| **P1** | HITO 2, HITO 3, HITO 4 | Monetización + estabilidad + observabilidad | Producción enterprise-ready |
| **P2** | HITO 5 Track A, B | Mercado global + integraciones | Escala internacional |
| **P3** | HITO 5 Track C, D | Seguridad avanzada + estratégicas futuras | Largo plazo (12+ meses) |

---

## 5. Estimación de Esfuerzo Total

| Hito | Propuestas | Horas | Dev-Days | Calendars | Status |
|---|---|---|---|---|---|
| **HITO 1** | 9 | 51 | 6.4 | 4 semanas | 🟡 En progress |
| **HITO 2** | 11 | 81 | 10.1 | 4 semanas | 🔲 Planeado |
| **HITO 3** | 8 | 94 | 11.8 | 6 semanas | 🔲 Planeado |
| **HITO 4** | 9 | 96 | 12 | 6 semanas | 🔲 Planeado |
| **HITO 5-A** | 5 | 54 | 6.8 | 6 semanas (Q3) | 🔲 Futuro |
| **HITO 5-B** | 6 | 70 | 8.8 | 8 semanas (Q3/Q4) | 🔲 Futuro |
| **HITO 5-C** | 4 | 54 | 6.8 | 8 semanas (Q3/Q4) | 🔲 Futuro |
| **HITO 5-D** | 4+ | 80+ | 10+ | TBD | 🔲 Futuro |

**Total Corto + Mediano (HITO 1-4):** 37 propuestas, 322 horas (~40 dev-days, ~8 semanas de sprint 5-day para 1 dev, **4 semanas para equipo de 2**)

---

## 6. Criterios de Éxito por Hito

### **HITO 1 ✅**
- [ ] `docs/product-design/` con 4 docs base (SITUACION_ACTUAL, ANALISIS_DOLORES, REQUERIMIENTOS, esta propuesta)
- [ ] Markdown references verificadas + CI enforcement activo
- [ ] Lint/formato automatizado en CI
- [ ] Redundancia signing key eliminada
- [ ] @PreAuthorize matriz completa en controllers
- [ ] Replay attack detection implementada
- [ ] Scope filtering en `/userinfo` funcional
- [ ] Documentación ARCHITECTURE.md, BOOTSTRAP_FILTER.md actualizada (no describe obsoleto)

### **HITO 2 ✅**
- [ ] Tests integración con Testcontainers ejecutables (JPA ↔ Flyway validation)
- [ ] Dashboard platform cacheado (<500ms latencia)
- [ ] Tenant dashboard + stats endpoint activo
- [ ] Billing operacional: facturas consultables, validaciones completas
- [ ] Cobertura JaCoCo 50% → 65% (all modules)
- [ ] Script verificación seed V14 ejecutable (conteos esperados)

### **HITO 3 ✅**
- [ ] Gateway Stripe/MercadoPago integrado (test mode)
- [ ] Renovación automática suscripciones funcional (@Scheduled job)
- [ ] Dunning: reintentos + emails automáticos
- [ ] Cobertura `keygo-supabase` JaCoCo 60%+
- [ ] Endpoint facturas individual (`GET /billing/invoices/{id}`)

### **HITO 4 ✅**
- [ ] Contrato multi-dominio documentado + validación en código
- [ ] Patrón BFF ejemplo funcional en `/docs/examples/bff-login-example/`
- [ ] Audit trail tabla + API funcionando
- [ ] TraceId/requestId propagado en error responses
- [ ] Prometheus endpoint exportando métricas keygo_*
- [ ] Sesiones con geolocalización IP funcional

---

## 7. Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| **Gateway integración toma +tiempo** | 🟠 Media | 🔴 Alto (bloquea HITO 3) | Usar sandbox Stripe/MercadoPago temprano; mock completo hasta T-084 |
| **Tests integración flaky (DB)** | 🟠 Media | 🟠 Medio | Testcontainers + scripts de limpieza; retry policy |
| **Scope creep en HITO 1** | 🟡 Baja | 🟠 Medio | Strict prioritization; parquetizar T-XXX fuera de HITO 1 si requieren >8h |
| **Performance dashboard sin caché** | 🟡 Baja | 🟡 Bajo | Caché es P1; en el interim, documentar en wiki |
| **Cambios en SCIM spec futuro** | 🔵 Muy baja | 🔵 Bajo (largo plazo) | Revalidar T-047 Q3 2026 vs. RFC 7644 latest |

---

## 8. Próximos Pasos Inmediatos

### **Semana 1 (2026-04-05 → 2026-04-12)**
1. ✅ Completar Fase 2 (este doc + BOUNDED_CONTEXTS + GLOSARIO + DEPENDENCIAS)
2. ✅ Validar SITUACION_ACTUAL.md contra código actual
3. 🟡 Iniciar T-030 (verificación Markdown refs) en paralelo
4. 🟡 Configurar Checkstyle/Spotless (T-023 step 1)

### **Semana 2-4 (HITO 1 ejecución)**
1. 🔲 T-023 (lint/formato) complete
2. 🔲 T-030/T-031 (refs markdown + CI) complete
3. 🔲 V-067 (eliminar signing key redundante) complete
4. 🔲 T-051 (matriz @PreAuthorize) complete

### **Semana 5-8 (HITO 2 planeado)**
1. 🔲 T-091/T-013/T-025 tests integración iniciado
2. 🔲 T-074/T-075 caché + dashboard initiated

---

## 9. Relación con Bounded Contexts

| Contexto | Propuestas Incluidas | HITO Principal | Completitud Post-HITO4 |
|---|---|---|---|
| **Auth** | T-035, T-036, T-043, T-064, T-063, T-059 | 1, 4 | 95% ✅ |
| **Tenants** | T-051, T-070, T-075, T-057, T-058 | 1, 2, 4 | 95% ✅ |
| **Billing** | T-083, T-084, T-085, T-090, T-086, T-098, T-099 | 2, 3 | 90% ✅ |
| **Account** | T-108, T-109, T-046 [futuro] | 4 | 85% ✅ |
| **Platform** | T-074, T-075, T-070, T-073, T-076 | 2, 4 | 85% ✅ |
| **Integraciones** | T-047, T-048, T-044, T-045, T-028, T-055 | 5 | 0% (post-lanzamiento) |

---

## 10. Resumen Ejecutivo

### **Consolidación del Roadmap en 4 Hitos Estratégicos**
1. **HITO 1 (P0):** Documentación centralizada + deuda técnica → MVP lanzable (4 sem)
2. **HITO 2 (P1):** Tests + performance + billing operacional → Estabilidad (4 sem)
3. **HITO 3 (P1):** Gateway real + renovación automática → Monetización (6 sem)
4. **HITO 4 (P1):** Multi-dominio + observabilidad → Enterprise-ready (6 sem)

### **Effort Total**
- **Corto + Mediano plazo (HITO 1-4):** 322 horas (~40 dev-days)
- **Equivalente:** 4 semanas para equipo de 2 devs en paralelo
- **Calendario:** 2026-04-05 a 2026-08-26 (20 semanas de calendario, 4 de sprint)

### **Entregables Clave**
- ✅ Documentación centralizada (Fase 2, 3, 4 completa)
- ✅ Arquitectura consolidada (Bounded Contexts, Diagramas)
- ✅ MVP lanzable (Hosted Login + Account + Auth core)
- ✅ Monetización (Gateway real + Billing)
- ✅ Enterprise-ready (Observabilidad, Audit, Multi-dominio)

---

**Última actualización:** 2026-04-05  
**Propietario:** AI Agent + Equipo de Desarrollo  
**Próxima revisión:** 2026-04-20 (post-HITO-1 inicial)
