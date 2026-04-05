# Matriz de Dependencias Entre Propuestas — KeyGo Server

> **Descripción:** Mapeo explícito de qué propuestas (T-NNN/F-NNN) bloquean a otras. Facilita priorización y paralelización.

**Fecha:** 2026-04-05

---

## 1. Dependencias Críticas (Critical Path)

```
T-091 (Test integración Testcontainers: validación JPA ↔ Flyway)
  ├── Bloquea: T-013 (tests integración keygo-supabase)
  ├── Bloquea: T-025 (tenant flow integration tests)
  └── Esfuerzo: 12h

T-013 (Tests integración keygo-supabase)
  ├── Depende de: T-091
  ├── Bloquea: [ninguno crítico]
  └── Esfuerzo: 16h

T-025 (Tests integración flujo tenant completo)
  ├── Depende de: T-091
  ├── Bloquea: [ninguno crítico]
  └── Esfuerzo: 10h

T-084 (Gateway integración Stripe/MercadoPago)
  ├── Depende de: [ninguno]
  ├── Bloquea: T-085 (renovación automática)
  ├── Bloquea: T-090 (dunning)
  └── Esfuerzo: 24h

T-085 (Renovación automática suscripciones)
  ├── Depende de: T-084
  ├── Bloquea: T-090 (dunning necesita renewal funcionando)
  └── Esfuerzo: 12h

T-090 (Motor dunning: reintentos)
  ├── Depende de: T-084, T-085
  ├── Bloquea: [monetización completa]
  └── Esfuerzo: 16h

T-074 (Caché dashboard)
  ├── Depende de: [ninguno]
  ├── Bloquea: T-075 (dashboard tenant requiere caché)
  └── Esfuerzo: 6h

T-075 (Dashboard tenant-específico)
  ├── Depende de: T-074
  ├── Bloquea: [ninguno crítico]
  └── Esfuerzo: 8h

T-076 (Audit trail: tabla + API)
  ├── Depende de: [ninguno]
  ├── Bloquea: [observabilidad completa, futuro]
  └── Esfuerzo: 16h
```

---

## 2. Matriz Completa de Dependencias

### **Propuestas de Corto Plazo (HITO 1-2)**

| ID | Propuesta | Depende De | Bloquea | Est. | Hito |
|---|---|---|---|---|---|
| **V-067** | Eliminar SigningKeyBootstrapService | — | — | 2h | 1 |
| **T-023** | Lint/formato (Checkstyle) | — | T-031 (CI automation) | 8h | 1 |
| **T-030** | Verificación refs Markdown | — | T-031 (CI enforcement) | 4h | 1 |
| **T-031** | Automatizar link check en CI | T-030 | — | 6h | 1 |
| **T-051** | Matriz @PreAuthorize | — | — | 12h | 1 |
| **T-035** | Replay attack detection | — | — | 6h | 1 |
| **T-062** | Handler MissingServletRequestParameter | — | — | 4h | 1 |
| **T-036** | TTL configurable refresh tokens | — | — | 3h | 1 |
| **T-043** | Scope filtering /userinfo | — | — | 6h | 1 |
| **T-091** | Test integración Testcontainers | — | T-013, T-025 | 12h | 2 |
| **T-013** | Tests integración keygo-supabase | T-091 | — | 16h | 2 |
| **T-025** | Tests integración flujo tenant | T-091 | — | 10h | 2 |
| **T-074** | Caché dashboard | — | T-075 | 6h | 2 |
| **T-075** | Dashboard tenant-específico | T-074 | — | 8h | 2 |
| **T-070** | Stats tenant con filtros fecha | — | — | 8h | 2 |
| **T-053** | Script verificación seed V14 | — | — | 4h | 2 |
| **T-083** | Endpoint GET /invoices/{id} | — | — | 4h | 2 |
| **T-094** | Tests AppPlanBillingOptionRepositoryAdapter | — | — | 6h | 2 |
| **T-095** | Validar ≥1 isDefault en billingOptions | — | — | 3h | 2 |
| **T-096** | @NotNull/@Valid en CreateAppPlanRequest | — | — | 4h | 2 |

### **Propuestas de Mediano Plazo (HITO 3-4)**

| ID | Propuesta | Depende De | Bloquea | Est. | Hito |
|---|---|---|---|---|---|
| **T-084** | Gateway real (Stripe/MercadoPago) | — | T-085, T-090, T-086 | 24h | 3 |
| **T-085** | Renovación automática | T-084 | T-090 | 12h | 3 |
| **T-090** | Dunning (reintentos) | T-084, T-085 | — | 16h | 3 |
| **T-086** | Bearer TENANT_USER en /subscription | T-084 [opcional] | — | 6h | 3 |
| **T-098** | Filtro subscriberType catalog | — | — | 4h | 3 |
| **T-099** | Caché GetAppPlanCatalogUseCase | — | — | 4h | 3 |
| **T-097** | PUT /billing/plans/{code}/options | — | — | 8h | 3 |
| **T-115** | Cobertura JaCoCo keygo-supabase 60% | — | — | 20h | 3 |
| **T-057** | Contrato multi-dominio | — | T-058 (referencia) | 12h | 4 |
| **T-058** | Patrón BFF login | T-057 [opcional] | — | 16h | 4 |
| **T-063** | TraceId/requestId en ErrorData | — | T-066 | 6h | 4 |
| **T-066** | endpointHint/actionHint | T-063 [opcional] | — | 4h | 4 |
| **T-064** | Catálogo i18n avanzado | — | — | 10h | 4 |
| **T-108** | Geolocalización IP sesiones | — | — | 10h | 4 |
| **T-109** | Job cleanup sesiones | T-108 [opcional] | — | 8h | 4 |
| **T-073** | Micrometer + Prometheus | — | — | 14h | 4 |
| **T-076** | Audit trail (tabla + API) | — | — | 16h | 4 |

### **Propuestas Largo Plazo (HITO 5 — Post-Lanzamiento)**

| ID | Propuesta | Depende De | Bloquea | Est. | Hito |
|---|---|---|---|---|---|
| **T-044** | Membership attributes metadata | — | T-045 | 8h | 5-A |
| **T-045** | Claim mappers por ClientApp | T-044 | — | 10h | 5-A |
| **T-047** | SCIM 2.0 aprovisionamiento | T-048 [opcional] | — | 20h | 5-A |
| **T-048** | Custom attributes schema | — | T-047 (mejora) | 12h | 5-A |
| **T-055** | Bootstrap programático control-plane | — | — | 14h | 5-A |
| **T-089** | Multi-moneda (exchange rates) | — | T-101 | 12h | 5-B |
| **T-100** | Precios tiers/escalonado | — | — | 10h | 5-B |
| **T-101** | Overrides precio por moneda | T-089 | T-102 (mejora) | 8h | 5-B |
| **T-102** | Precios dinámicos webhook | T-101 [opcional] | — | 12h | 5-B |
| **T-087** | PDF de facturas | T-084 [opcional] | T-088 (CFDI) | 12h | 5-B |
| **T-088** | CFDI México (factura electrónica) | T-087, T-089 | — | 16h | 5-B |
| **T-028** | KMS externo | — | — | 18h | 5-C |
| **T-038** | Lista negra JTI revocación inmediata | — | — | 10h | 5-C |
| **T-020** | OpenTelemetry avanzada | — | — | 16h | 5-C |
| **T-059** | Redirect OAuth2 clásico (302) | — | — | 10h | 5-C |
| **F-041** | SSO multi-app (sesión compartida) | F-030 [completada] | — | 32h | 5-D |
| **F-042** | Account connections | — | — | 16h | 5-A |
| **F-040** | RBAC granular por permiso | — | — | 20h | 5-D |
| **T-032** | Generador site estático | — | — | 12h | 5-D |

---

## 3. Gráfico de Rutas Críticas

### **HITO 1: Docs + Deuda (0 bloqueadores)**
```
All proposals independent:
V-067, T-023→T-031, T-030, T-051, T-035, T-036, T-043, T-062

PARALLELIZABLE: 100% (ejecutar todo en paralelo)
```

### **HITO 2: Estabilidad (1 bloqueador principal)**
```
T-091 ──┬──→ T-013
        ├──→ T-025
        
T-074 ──→ T-075

T-070, T-053, T-083, T-094, T-095, T-096 [independent]

PARALLELIZABLE: 80% (T-091 es cuello de botella por 12h)
```

### **HITO 3: Monetización (2 bloqueadores críticos)**
```
T-084 ──┬──→ T-085 ──→ T-090
        ├──→ T-086
        
T-097, T-098, T-099, T-115 [independent]

CRITICAL PATH: T-084 → T-085 → T-090 (52h en serie)
PARALLELIZABLE: 60% (T-086/T-097/T-098/T-099/T-115 en paralelo con T-084)
```

### **HITO 4: Enterprise (0 bloqueadores de ejecución, todos parallelizable)**
```
T-057 ────┐
T-058 ────┤
T-063 ──┬─┤
T-066 ──┘ │
T-064 ────┤
T-108 ──┬─┤
T-109 ──┘ │
T-073 ────┤
T-076 ────┘

PARALLELIZABLE: 100% (ejecutar todo en paralelo)
CRITICAL PATH: máx 16h (T-058, T-090, T-076)
```

---

## 4. Análisis de Paralelización

### **HITO 1: 51 horas en 6.4 dev-days**
- **Si 1 dev:** 51h = 6.4 days
- **Si 2 devs en paralelo:** 51h ÷ 2 = 25.5h = 3.2 days (teórico)
- **Realidad (overhead):** 4 days
- **Recomendación:** PARALLELIZAR TODO; T-023/T-031 sequential (4h + 6h = 10h), resto en paralelo

### **HITO 2: 81 horas en 10 dev-days**
- **Si 1 dev:** 81h = 10 days
- **Si 2 devs:** 81h ÷ 2 = 40.5h = 5 days (teórico); realidad ~6 days
- **Cuello de botella:** T-091 (12h) bloquea T-013 (16h) y T-025 (10h)
- **Strategy:** 
  - Dev 1: T-091 (12h) → T-013 (16h) = 28h
  - Dev 2: T-074 (6h) → T-075 (8h) → T-070/T-053/T-083/T-094/T-095/T-096 = 53h
  - **Total secuencial:** 28h vs 53h → balancear tareas

### **HITO 3: 94 horas en 11.8 dev-days**
- **Si 1 dev:** 94h = 11.8 days
- **Si 2 devs:** 94h ÷ 2 = 47h = 5.9 days (teórico); realidad ~6-7 days
- **Cuello de botella:** T-084 (24h) → T-085 (12h) → T-090 (16h) = 52h en serie
- **Strategy:**
  - Dev 1: T-084 (24h) → T-085 (12h) → T-090 (16h) = 52h (critical path)
  - Dev 2: T-086/T-097/T-098/T-099/T-115 = 42h en paralelo
  - **Total con overlap:** ~52h (limitado por critical path)

### **HITO 4: 96 horas en 12 dev-days**
- **Si 1 dev:** 96h = 12 days
- **Si 2 devs:** 96h ÷ 2 = 48h = 6 days (teórico); realidad ~7 days
- **Cuello de botella:** Ninguno (todas independientes)
- **Strategy:** PARALELIZAR COMPLETAMENTE; máx 16h (cualquier propuesta grande)

---

## 5. Propuestas Independientes (Pueden Iniciarse Anytime)

| ID | Descripción | Horizonte | Esfuerzo |
|---|---|---|---|
| T-036 | TTL configurable refresh tokens | Corto | 3h |
| T-043 | Scope filtering /userinfo | Corto | 6h |
| T-062 | Handler MissingServletRequestParameter | Corto | 4h |
| T-051 | Matriz @PreAuthorize | Corto | 12h |
| T-035 | Replay attack detection | Corto | 6h |
| T-053 | Script verificación seed | Mediano | 4h |
| T-070 | Stats tenant | Mediano | 8h |
| T-083 | Endpoint GET /invoices/{id} | Mediano | 4h |
| T-094 | Tests AppPlanBillingOption | Mediano | 6h |
| T-095 | Validar isDefault billing | Mediano | 3h |
| T-096 | @Valid/@NotNull tests | Mediano | 4h |
| T-086 | Bearer TENANT_USER billing | Mediano | 6h |
| T-097 | PUT /billing/plans options | Mediano | 8h |
| T-098 | Filtro subscriberType | Mediano | 4h |
| T-099 | Caché catalog | Mediano | 4h |
| T-063 | TraceId/requestId | Mediano | 6h |
| T-064 | Catálogo i18n avanzado | Mediano | 10h |
| T-108 | Geolocalización IP | Mediano | 10h |
| T-073 | Prometheus/Micrometer | Mediano | 14h |
| T-076 | Audit trail | Mediano | 16h |

---

## 6. Recomendación de Priorización Ágil

### **Sprint 1 (Semana 1-2): HITO 1 Inicio**
**Team size:** 2 devs  
**Propuestas:** V-067, T-023, T-030, T-031, T-051, T-035, T-062, T-036, T-043 (9 propuestas)  
**Estrategia:** PARALLELIZAR TODO  
**Deliverable:** Documentación completa + deuda técnica eliminada

### **Sprint 2 (Semana 3-4): HITO 2 Inicio + T-091 (bloqueador)**
**Team size:** 2 devs  
**Dev 1:** T-091 (12h) → T-013 (16h) = 28h  
**Dev 2:** T-074/T-075, T-070, T-053 = 26h (paralelo)  
**Deliverable:** Tests integración, dashboard cacheado

### **Sprint 3 (Semana 5-6): HITO 3 Inicio + Critical Path T-084**
**Team size:** 2 devs  
**Dev 1:** T-084 (24h) → T-085 (12h) → T-090 (16h) = 52h (critical path)  
**Dev 2:** T-086, T-097/T-098/T-099, T-115 = 42h (paralelo)  
**Deliverable:** Gateway real, renovación automática

### **Sprint 4 (Semana 7-8): HITO 4 (Enterprise)**
**Team size:** 2 devs  
**Estrategia:** PARALLELIZAR TODO (sin bloqueadores)  
**Propuestas:** T-057, T-058, T-063, T-064, T-066, T-108, T-109, T-073, T-076 (9)  
**Deliverable:** Multi-dominio, observabilidad, audit trail

---

## 7. Riesgos de Dependencias

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| **T-091 falla** | 🟡 Media | 🔴 Alto (bloquea T-013/T-025) | Setup Testcontainers temprano; prototipo en Sprint 1 |
| **T-084 delay** | 🟠 Media-Alta | 🔴 Alto (bloquea T-085/T-090/monetización) | Usar sandbox Stripe early; gateway interface prototipada antes |
| **T-085 complejidad** | 🟡 Baja | 🟠 Medio (impacta T-090) | Diseño @Scheduled job temprano; testear cron behavior |
| **T-090 scope creep** | 🟡 Media | 🟠 Medio (post-pone monetización) | Definir "dunning mínimo": reintentos D+1, D+3, D+7 nada más |
| **T-084 integración PCI** | 🟠 Media | 🔴 Alto (blocker legal) | Contactar Stripe/MP antes; reservar días buffer |

---

## 8. Resumen

### **Cuello de Botella Crítico**
- **HITO 3:** T-084 → T-085 → T-090 (52 horas en serie)
- **Recomendación:** Iniciar T-084 inmediatamente post-HITO-2

### **Mejor Estrategia de Equipo**
- **2 devs:** Paralelizar Sprints 1, 4 (100%); Sprints 2, 3 balancear critical path
- **1 dev:** Ejecutar secuencial HITO 1 → 2 → 3 → 4; total ~40 dev-days (8 semanas)
- **3+ devs:** Overkill; todos HITOs en paralelo (no hay trabajo >16h unidisciplinario)

### **Lecciones Arquitectónicas**
- **Menos dependencias = mejor:** T-084/T-085/T-090 deberían haberse diseñado independientes
- **Proposal:** Futuro refactor dunning como job independiente (T-090 sin depender T-084/T-085)

---

**Última actualización:** 2026-04-05  
**Próxima revisión:** Post-HITO-2 (validar velocidad real vs. estimado)
