# Análisis de Dolores y Restricciones — KeyGo Server

> **Contexto:** Problemas identificados en el proyecto, causas raíz, impacto y restricciones técnicas/funcionales.

**Fecha:** 2026-04-05

---

## 1. Dolores de Desarrollo

### **Dolor 1: Conocimiento Disperso 🧠**

**Síntoma:** Documentación en 40+ archivos sin hilo conductor. Propuestas (ROADMAP, propuestas.md) sin explicar el *por qué*. Nuevo dev tarda 2 semanas entendiendo estructura.

**Causa Raíz:**
- Proyecto evolucionó organicamente sin referencia única centralizada
- Decisiones de diseño (bounded contexts, entidades, flujos) no documentadas
- Análisis de dolores/restricciones implícito en código y commits

**Impacto:**
- ⏱️ Onboarding lento (~10-15 horas para contexto básico)
- 🐛 Errores por falta de contexto (refs rotas, patrones no seguidos)
- 📉 Velocidad de desarrollo reduce por re-aprendizaje

**Solución:** Este directorio `docs/product-design/` centraliza todo.

---

### **Dolor 2: Desalineación Roadmap-Código 🗺️**

**Síntoma:** 152 propuestas (T-NNN/F-NNN) pero equipo implementa por urgencia, no por priorización. Features parcialmente implementadas; propuestas completadas sin actualizar roadmap.

**Causa Raíz:**
- Roadmap es "wish list" sin dependencias explícitas (¿qué bloquea qué?)
- Sin dueño visible por contexto (¿quién es responsable de Auth?, ¿de Billing?)
- Propuestas de largo plazo (T-059, T-064, T-020) no descompuestas en pasos

**Impacto:**
- 🔀 Trabajo ad-hoc, no estratégico
- 🚧 Propuestas mediano/largo plazo congeladas (3+ meses sin progreso)
- 💔 Frustración: "no progresamos hacia arquitectura objetivo"

**Solución:** PROPUESTA_SOLUCION.md + DEPENDENCIAS.md mapean rutas críticas y bloqueadores.

---

### **Dolor 3: Inconsistencias Docs ↔ Código 🔴**

**Síntoma:** `ARCHITECTURE.md` describe `X-KEYGO-ADMIN` cuando la realidad es `Authorization: Bearer` desde 2026-03-25. `BOOTSTRAP_FILTER.md` con rutas públicas incompletas. Docs dice JPA **Specifications** pero adaptadores antiguos en caché.

**Causa Raíz:**
- Documentación no actualizada post-refactor
- Sin proceso de validación docs-vs-código pre-commit
- Sin script CI que verifique consistencia (lychee/markdown-link-check no configurados)

**Impacto:**
- 😕 Confusión al leer docs obsoleta
- 🐛 Copy-paste de ejemplos que no funcionan
- 💢 Pérdida de confianza en documentación

**Solución:** Este producto-design es fuente de verdad única. Pre-commit hook (futuro T-031).

---

### **Dolor 4: Falta de Diagramas Visuales 📊**

**Síntoma:** Casos de uso descritos en texto (prosa). Flujos OAuth2 en `AUTH_FLOW.md` sin diagrama. Máquinas de estado (usuario ACTIVE → RESET_PASSWORD → ACTIVE) solo en código JPA.

**Causa Raíz:**
- Sin herramienta preferida de diagramas definida
- PlantUML / Mermaid no configurados en CI
- Texto es "más fácil" que mantener SVG

**Impacto:**
- 🧩 Difícil entender flujos complejos (password reset: 4 pasos)
- 🔀 Ambigüedad en handoff entre contextos (¿cuándo pasa estado del usuario?)
- 📚 Onboarding requiere reading code para entender sequences

**Solución:** DIAGRAMAS/ con Mermaid (cases, flows, sequences, state machines).

---

## 2. Dolores de Producto

### **Dolor 5: Billing sin Gateway Real 💳**

**Síntoma:** Endpoint `/mock-approve-payment` hardcoded. No integración Stripe/MercadoPago. Imposible vender KeyGo hoy; solo demo.

**Causa Raíz:**
- Funcionalidad MVP entregable en tiempo (T-086 Q2 2026)
- Gateway requiere PCI compliance, contratos, testing de integración
- Modelo económico no validado aún

**Impacto:**
- 🚫 No monetización posible
- ⏱️ Bloquea propuestas downstrem (renovación automática T-085, dunning T-090, CFDI T-088)

**Solución:** T-084 (mediano plazo), descompuesto en T-084 (gateway), T-085 (renewal), T-086 (soporte TENANT_USER bearer).

---

### **Dolor 6: Sin Multi-Moneda / Precios Dinámicos 💱**

**Síntoma:** Todos los precios en una moneda base. No hay soporte para MXN/EUR/JPY. Clientes globales quedan fuera.

**Causa Raíz:**
- MVP usa moneda única (decisión: Largo plazo)
- Tipos de cambio requieren tabla, API externa, o fijación manual
- Modelos de pricing (tiers, dinámicos) requieren arquitectura nueva

**Impacto:**
- 🌍 Mercado limitado (solo USD)
- 💰 Margen reducido (conversiones externas, comisiones)

**Solución:** T-089 (multi-moneda), T-100 (pricing tiers), T-102 (precios dinámicos) — Largo plazo.

---

### **Dolor 7: Sin Integración SCIM / Aprovisionamiento 👥**

**Síntoma:** Admin de tenant crea usuarios manualmente vía API. HR/Okta/OneLogin no pueden auto-provisionar empleados.

**Causa Raíz:**
- SCIM 2.0 es estándar pero no MVP
- Requiere mapeo de atributos personalizados (T-048)
- Roadmap = Q3 2026

**Impacto:**
- 🔄 Setup manual tedioso para tenants grandes
- 📋 No integración con sistemas HR existentes

**Solución:** T-047 (SCIM 2.0) + T-048 (custom attributes) — Largo plazo Q3.

---

## 3. Restricciones Técnicas

### **Restricción T1: Stack Fijo (Java 21 + Spring Boot 4.x + PostgreSQL)**

**Descripción:** Arquitectura base no es negociable. No migramos a Go, Node, Rust.

**Impacto:** 
- Decisiones deben alinearse con Spring ecosystem
- ORM es JPA (no Hibernate custom)
- Observabilidad usa Micrometer (no Jaeger custom)

**Referencia:** ARCHITECTURE.md § "Stack"

---

### **Restricción T2: Migraciones Flyway (Versionado Inmutable)**

**Descripción:** Una vez aplicada, una migración no se edita. Si hay error, se crea V-siguiente.

**Regla:**
```
✅ CORRECTO:    V24__add_table.sql → error → V25__fix_table_add_column.sql
❌ INCORRECTO:  editar V24__add_table.sql post-aplicación
```

**Impacto:**
- Historial de migraciones es "escribir una sola vez"
- Facilita reproducibilidad pero requiere testing previo
- Schema debe validarse en CI contra DB de prueba

**Referencia:** `docs/data/MIGRATIONS.md`

---

### **Restricción T3: `keygo-domain` sin Spring**

**Descripción:** Dominio puro = sin anotaciones Spring, sin dependencias `@SpringBootTest`, sin Autowired.

**Regla:**
```java
❌ INCORRECTO:  public class Tenant { @Id private Long id; }
✅ CORRECTO:    public record TenantId(Long value) { }
```

**Impacto:**
- Dominio es testeable sin framework
- Cambio de persistencia no afecta dominio
- Requiere VO (value objects) en lugar de entidades JPA directas

**Referencia:** CLAUDE.md § "Reglas Críticas"

---

### **Restricción T4: Jackson 3 (namespace tools.jackson)**

**Descripción:** Spring Boot 4.x usa Jackson 3; imports cambiaron.

**Regla:**
```java
✅ CORRECTO:    import tools.jackson.databind.json.JsonMapper;
❌ INCORRECTO:  import com.fasterxml.jackson.databind.ObjectMapper;
```

**Impacto:**
- Código viejo copy-pasted fallará
- IntelliJ auto-complete propone import incorrecto (configurar code style)

**Referencia:** AI_CONTEXT.md § "Jackson 3"

---

### **Restricción T5: `context-path=/keygo-server` Obligatorio**

**Descripción:** Todos los endpoints incluyen prefijo `/keygo-server`.

**Regla:**
```
✅ GET /keygo-server/api/v1/tenants
❌ GET /api/v1/tenants
```

**Impacto:**
- URLs hardcodeadas en cliente
- Documentación (Postman, OpenAPI) debe incluir prefijo
- Proxies/gateways deben stripear ruta correctamente

**Referencia:** AGENTS.md § "context-path is always active"

---

### **Restricción T6: Seguridad Bearer-Only (No X-KEYGO-ADMIN)**

**Descripción:** Desde 2026-03-25, autenticación es `Authorization: Bearer <jwt>` exclusivamente. Header `X-KEYGO-ADMIN` obsoleto.

**Regla:**
```http
✅ CORRECTO:    Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
❌ INCORRECTO:  X-KEYGO-ADMIN: token
```

**Impacto:**
- RBAC por endpoint vía `@PreAuthorize`
- Tenant resolution automática desde JWT claim `iss` (tenant_slug)
- Docstring `@PreAuthorize` debe listar roles permitidos

**Referencia:** ARCHITECTURE.md § "Seguridad"

---

## 4. Restricciones Funcionales

### **Restricción F1: Multi-Tenant Aislamiento por Path**

**Descripción:** Tenant se resuelve desde path variable `/{tenantSlug}/`. No se mezcla data entre tenants.

**Regla:**
```
GET /keygo-server/api/v1/tenants/{tenantSlug}/users
    → Solo usuarios del tenant {tenantSlug}, nunca otro
```

**Impacto:**
- Queries JPA siempre filtran por `tenant_id` (no opcional)
- Errores de isolamiento = violación de seguridad P0
- Tests deben validar "usuario de tenant A no ve users de tenant B"

**Referencia:** ARCHITECTURE.md § "Multi-Tenant Strategy"

---

### **Restricción F2: OIDC Claims Estándar**

**Descripción:** JWT `id_token` incluye claims estándar OIDC (name, email, picture, phone, birthdate, locale, updated_at).

**Regla:**
```json
{
  "sub": "user-uuid",
  "iss": "https://keygo.local/tenants/acme",
  "aud": "client-id",
  "name": "John Doe",
  "email": "john@acme.com",
  "email_verified": true,
  "picture": "https://...",
  "phone_number": "+1234567890",
  "birthdate": "1990-01-15",
  "locale": "es-MX",
  "updated_at": 1712329500
}
```

**Impacto:**
- Claims no estándar van a `app_plan_entitlements` o metadata (futuro T-044, T-045)
- Mapeo de claims configurables por app (futuro T-045)

**Referencia:** docs/api/AUTH_FLOW.md § "ID Token Structure"

---

### **Restricción F3: Sesiones Stateless (No Server-Side)**

**Descripción:** Una vez JWT emitido, no hay sesión server-side. Revocación es por lista negra (futuro Redis, T-038).

**Regla:**
```
Usuario login → JWT emitido con exp=1h
Usuario logout → /oauth2/revoke actualiza refresh_token status=REVOKED
Pero: access_token sigue siendo válido hasta exp
```

**Impacto:**
- Revocación no es inmediata (espera expiración de access token)
- Clientes deben manejar token expiration (401 → refresh o logout)
- Caché JTI (T-038) permite revocación inmediata (Largo plazo)

**Referencia:** ARCHITECTURE.md § "Session Strategy"

---

## 5. Matriz de Dolores vs. Propuestas

| Dolor | Propuestas Asociadas | Horizonte | Prioridad |
|---|---|---|---|
| **Conocimiento disperso** | (Este doc: product-design) | Inmediato | 🔴 P0 |
| **Desalineación roadmap-código** | T-030, T-031, PROPUESTA_SOLUCION.md | Corto plazo | 🔴 P0 |
| **Inconsistencias docs** | T-030, T-031, ARQUITECTURA.md update | Corto plazo | 🟠 P1 |
| **Sin diagramas visuales** | (Este doc: DIAGRAMAS/) | Inmediato | 🟠 P1 |
| **Billing sin gateway** | T-084, T-085, T-090 | Mediano plazo | 🔴 P1 |
| **Sin multi-moneda** | T-089, T-100, T-101, T-102 | Largo plazo | 🟡 P2 |
| **Sin SCIM** | T-047, T-048 | Largo plazo | 🟡 P2 |
| **Redundancia Signing Key** | V-067 | Corto plazo | 🟠 P1 |
| **Sin detección replay** | T-035 | Corto plazo | 🟠 P1 |
| **Sin lint enforced** | T-023, T-031 | Mediano plazo | 🟡 P2 |

---

## 6. Restricciones Regulatorias / Compliance

### **GDPR (Futuro T-XXX)**
- ✅ Actual: Datos de usuario en PostgreSQL (data residency configurable por env)
- 🔲 Faltante: Right to be forgotten (borrado de datos), data export, consent management

### **CFDI México (Futuro T-088)**
- Facturas electrónicas para clientes MXN
- Requiere integración con PAC (Proveedor Autorizado Certificación)
- Impacto: 3 propuestas futuras (T-087 PDF, T-088 CFDI, T-090 dunning)

### **PCI DSS (Para Billing T-084)**
- Gateway real (Stripe/MercadoPago) = PCI Level 1 (no almacenar números tarjeta)
- KeyGo nunca maneja números de tarjeta directamente (delegado a gateway)

---

## 7. Resumen Ejecutivo

### **Dolores Principales (4)**
1. ❌ **Conocimiento disperso** — 40+ docs sin hilo. Solución: product-design centraliza todo.
2. ❌ **Desalineación roadmap** — 152 propuestas sin dependencias. Solución: PROPUESTA_SOLUCION + DEPENDENCIAS mapean.
3. ❌ **Inconsistencias docs vs. código** — ARCHITECTURE.md describe seguridad obsoleta. Solución: este doc es fuente de verdad.
4. ❌ **Sin diagramas visuales** — Flujos solo en texto. Solución: DIAGRAMAS/ con Mermaid.

### **Restricciones Técnicas (6)**
- Java 21 + Spring Boot 4.x (fijo)
- Flyway versionado inmutable (regla de oro)
- keygo-domain sin Spring (arquitectura hexagonal)
- Jackson 3 namespace tools.jackson (no com.fasterxml)
- context-path=/keygo-server (obligatorio)
- Bearer-only auth (X-KEYGO-ADMIN obsoleto)

### **Restricciones Funcionales (3)**
- Multi-tenant aislamiento por path (P0 seguridad)
- OIDC claims estándar (OIDC compliance)
- Sesiones stateless vía JWT (no server-side)

### **Dolores de Producto (3)**
1. **Billing sin gateway real** (bloquea monetización, Mediano plazo)
2. **Sin multi-moneda** (mercado limitado, Largo plazo)
3. **Sin SCIM** (aprovisionamiento manual, Largo plazo)

---

**Última actualización:** 2026-04-05  
**Propuesta:** Resolver dolores 1-4 en Corto plazo (2-4 semanas); dolores técnicos paralelo.
