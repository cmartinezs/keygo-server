# RFC Closure Process — Ciclo de Vida de RFCs y Absorción en Canon

**Propósito:** Definir cómo una RFC se cierra, sus decisiones se absorben en documentación canónica, y se archivan para referencia histórica.

---

## Problema: RFCs Huérfanas

**Síntoma:** 26 archivos de RFC en `/docs/rfc/` (restructure-multitenant, account-ui-proposal, t108-geoip-sessions, incomplete-sections) sin conexión con ARCHITECTURE.md, PATTERNS.md, o documentación temática.

**Impacto:**
- Nuevo dev lee ARCHITECTURE.md pero no conoce decisions en RFCs
- Múltiples fuentes de verdad (RFC vs canon)
- RFCs acumulan metadata sin consumir decisiones
- Al refactorizar, no se sabe si hay RFC que invalidó cambio

**Solución:** Proceso explícito de RFC → Canon → Archive.

---

## Ciclo de Vida de una RFC

```
1. DRAFT
   └─ Escrita en /docs/rfc/{name}/
      Propone decisión arquitectónica

2. DISCUSSION  (optional)
   └─ Feedback de equipo
      Refinada en el mismo folder

3. APPROVED
   └─ Decisión tomada
      Status: Ready to implement

4. IMPLEMENTED
   └─ Código escrito
      Status: deployed to production

5. CLOSURE (← THIS PROCESS)
   └─ Decisiones absorbidas en canon
      Status: documented in ARCHITECTURE.md, PATTERNS.md, etc.

6. ARCHIVED
   └─ RFC movida a /docs/archive/rfc/
      Status: reference only, decisions are in canon
```

---

## Fase de Cierre: Absorción en Canon

Cuando una RFC es IMPLEMENTED y estable en producción, iniciar closure:

### Paso 1: Identificar Decisiones Clave

Leer RFC completa y listar decisiones arquitectónicas:

**Ejemplo: restructure-multitenant RFC**
```
Decisión 1: "Identidad es global, pertenencia es por tenant, acceso es por app"
  → Impacta: ARCHITECTURE.md § Model de Identidad
  → Impacta: PATTERNS.md § Agregados

Decisión 2: "Tenant interno `keygo` NO es universal"
  → Impacta: ARCHITECTURE.md § Multi-tenancy
  → Impacta: PATTERNS.md § Scoping

Decisión 3: "JWT contiene tenant_slug en claim"
  → Impacta: docs/design/AUTHORIZATION_PATTERNS.md

Decisión 4: "Hosted login depende de sesión global"
  → Impacta: Operaciones, deployment (PRODUCTION_RUNBOOK.md)
```

### Paso 2: Mapear a Documentos Canónicos

| RFC Decisión | Documento Canon | Sección | Acción |
|---|---|---|---|
| Identidad global vs tenant | ARCHITECTURE.md | § Model | Expandir con diagrama de RFC |
| `keygo` no universal | ARCHITECTURE.md | § Multi-tenancy | Agregar limitación explícita |
| JWT + tenant_slug | AUTHORIZATION_PATTERNS.md | § JWT Claims | Documentar |
| Hosted login sesión | ../operations/PRODUCTION_RUNBOOK.md | § Deployment | Documentar |

### Paso 3: Escribir Cambios en Canon

Para cada mapeo, crear/actualizar documento canónico:

**Ejemplo A: Actualizar ARCHITECTURE.md**
```markdown
## Model de Identidad (de RFC restructure-multitenant §02-modelo-identidad-multitenancy.md)

Keygo adopta el principio: **identidad es global, pertenencia es por tenant, acceso es por app**.

### Capas Conceptuales

| Capa | Entidad | Responsabilidad |
|---|---|---|
| Global | User | Credenciales, MFA, identidad única |
| Tenant | TenantUser | Membresía organizacional, perfil OIDC |
| App | Membership | Acceso funcional, roles por app |

(Diagrama de RFC aquí)

### Implementación

Código mapea perfectamente al modelo:
- `keygo-domain/user/model/User.java` — identidad global
- `keygo-domain/tenant/model/Tenant.java` — tenant scope
- `keygo-domain/membership/model/Membership.java` — app access

Ver: RFC restructure-multitenant §02 para fundamentación detallada.
```

**Ejemplo B: Crear AUTHORIZATION_PATTERNS.md (Sprint 2)**
```markdown
## JWT Claims Structure (de RFC restructure-multitenant §06-jwt-y-autorizacion.md)

Cada JWT contiene claims:
- `sub` — user UUID (global)
- `tenant_slug` — tenant actual
- `aud` — audience (client app)
- `roles` — roles en ese tenant/app

(Implementación detallada del RFC)
```

### Paso 4: Referenciar RFC Original

En documentos canónicos, agregar referencia:

```markdown
## Justificación

**Decisión tomada:** RFC estructural [`restructure-multitenant`](rfcs/restructure-multitenant/02-modelo-identidad-multitenancy.md)  
**Estado:** Implementado (92% alineado con propuesta)  
**Fecha closure:** 2026-04-10  
**Impactado por:** T-xyz (ticket de implementación)
```

### Paso 5: Mover RFC a Archive

Cuando cierre esté completo:

```bash
# Mover carpeta entera a archive con timestamp
mv docs/rfc/restructure-multitenant \
   docs/archive/rfc/restructure-multitenant-closure-2026-04-10

# Crear archivo de cierre
cat > docs/archive/rfc/RFC_CLOSURE_LOG.md << EOF
## restructure-multitenant

**Fecha cierre:** 2026-04-10  
**Status:** ✅ CLOSED (decisions absorbed in canon)  
**Documentos impactados:**
- ARCHITECTURE.md § Model de Identidad
- ARCHITECTURE.md § Multi-tenancy  
- docs/design/AUTHORIZATION_PATTERNS.md (nuevo)
- docs/design/PRODUCTION_RUNBOOK.md (nuevo)

**Referencia original:** docs/archive/rfc/restructure-multitenant-closure-2026-04-10/

**Síntesis:** 
Arquitectura multi-tenant de KeyGo está 92% alineada con RFC. Decisiones clave 
(identidad global, pertenencia por tenant, acceso por app) están implementadas 
en código y ahora documentadas en canon. RFC archivada como referencia histórica.
EOF
```

---

## Matriz: RFCs Pendientes de Cierre

| RFC | Carpeta | Status | Canon Target | Prioridad |
|---|---|---|---|---|
| **restructure-multitenant** | `/docs/rfc/restructure-multitenant/` | IMPLEMENTED (92%) | ARCHITECTURE.md + AUTHORIZATION_PATTERNS.md | 🔴 HIGH |
| **account-ui-proposal** | `/docs/rfc/account-ui-proposal/` | PARTIAL (UI done, backend partial) | ENDPOINT_CATALOG.md + (update) FRONTEND_DEVELOPER_GUIDE.md | 🟡 MEDIUM |
| **t108-geoip-sessions** | `/docs/rfc/t108-geoip-sessions/` | RFC only (not implemented) | Future: PRODUCTION_RUNBOOK.md + OBSERVABILITY.md | 🟢 LOW |
| **incomplete-sections** | `/docs/rfc/incomplete-sections/` | Work in progress | Various per section | 🟡 MEDIUM |

---

## Checklist: Cerrar una RFC

Cuando estés listos para cerrar una RFC:

- [ ] RFC está IMPLEMENTED (código en prod o merged)
- [ ] RFC está STABLE (no cambios esperados en próximas 2 semanas)
- [ ] Identificadas todas las decisiones clave (paso 1)
- [ ] Mapeadas a documentos canónicos (paso 2)
- [ ] Escribidos cambios en canon (paso 3)
- [ ] Agregadas referencias RFC en canon (paso 4)
- [ ] RFC movida a `/docs/archive/rfc/` (paso 5)
- [ ] Entrada agregada en RFC_CLOSURE_LOG.md
- [ ] Links roto verificados (RFC_CLOSURE_LOG.md → docs/archive/rfc/{name}/)

---

## Checklist: Escribir una Nueva RFC

Cuando escribas una nueva RFC:

- [ ] Créala en `/docs/rfc/{short-name}/` con README.md
- [ ] Usa estructura: `00-contexto.md`, `01-decisiones.md`, `02-diseno.md`, etc.
- [ ] Incluye sección "**Impacto esperado en canon**" al final
  ```
  ## Impacto en Documentación Canónica
  
  Al implementar esta RFC, actualizar:
  - ARCHITECTURE.md § [section]
  - docs/design/patterns/[name].md § [section]
  - (etc)
  ```
- [ ] Incluye sección "**Cómo cerrar esta RFC**" al final
  ```
  ## Closure Procedure (when implemented)
  
  1. Absorb decisions in ARCHITECTURE.md
  2. Create docs/design/[PATTERN_NAME].md if new pattern
  3. Update ENDPOINT_CATALOG.md if new endpoints
  4. Move this RFC to docs/archive/rfc/[name]-closure-YYYY-MM-DD/
  5. Aggregate entry in RFC_CLOSURE_LOG.md
  ```
- [ ] Agrégala a esta matriz (ROADMAP.md § RFC Pending)

---

## Template: Nueva RFC

```markdown
# RFC: {Title}

**Autor:** {Name}  
**Fecha:** {YYYY-MM-DD}  
**Estado:** DRAFT / DISCUSSION / APPROVED / IMPLEMENTED  

## Resumen Ejecutivo

{1-2 párrafos sobre la decisión y por qué importa}

## Problema

{Descripción del problema que la RFC resuelve}

## Solución Propuesta

{Detalles de la solución}

## Decisiones Clave

| # | Decisión | Rationale | Impacto |
|---|---|---|---|
| D1 | {statement} | {why} | {what changes} |
| D2 | ... | ... | ... |

## Impacto en Documentación Canónica

Cuando se implemente, actualizar:
- Document A § Section X
- (new) Document B with topic Y
- (etc)

## Closure Procedure (when implemented)

1. Absorb D1 in ARCHITECTURE.md § X
2. Absorb D2 in docs/design/[name].md § Y
3. (etc)
4. Move RFC to docs/archive/rfc/[name]-closure-YYYY-MM-DD/
5. Aggregate closure in RFC_CLOSURE_LOG.md

---

{detailed sections of the RFC}
```

---

## Anti-Patterns: Evitar

❌ **Mantener RFC sin closure**
```
# MAL
RFC escrita → Implementada → Olvidada en /docs/rfc/
Nuevo dev encuentra RFC y canon, no sabe cuál es verdad
```

✅ **RFC con closure y archive**
```
# BIEN
RFC escrita → Implementada → Cerrada con decisiones en canon
RFC archivada con referencia clara
Nuevo dev lee canon y sabe dónde está el "por qué"
```

---

❌ **Canon y RFC contradiciendo**
```
# MAL
ARCHITECTURE.md dice "use JWT"
RFC en /docs/rfc/auth-strategy dice "use sessions"
Cuál es verdad?
```

✅ **Canon único con RFC histórica**
```
# BIEN
ARCHITECTURE.md dice "use JWT" (decidido en RFC auth-strategy §02, closed 2026-04-10)
RFC archivada como contexto para decisión
```

---

## Referencias

| Recurso | Ubicación |
|---|---|
| **RFCs Activas** | `/docs/rfc/` |
| **RFCs Archivadas** | `/docs/archive/rfc/` |
| **Cierre Log** | `/docs/archive/rfc/RFC_CLOSURE_LOG.md` |
| **ROADMAP** | `/ROADMAP.md` (incluir RFCs pending) |
| **ARCHITECTURE** | `/ARCHITECTURE.md` (source of truth para decisiones) |

---

## Acción Inmediata: Matriz Priorizada

**Sprint 2 (esta semana):**
- [ ] Cerrar **restructure-multitenant** RFC → ARCHITECTURE.md + AUTHORIZATION_PATTERNS.md
- [ ] Cerrar **account-ui-proposal** RFC (parcial) → ENDPOINT_CATALOG.md
- [ ] Crear checklist de RFCs para futuros devs

**Sprint 3:**
- [ ] Cerrar **incomplete-sections** RFC → varios docs
- [ ] Evaluar **t108-geoip-sessions** para implementación o archive

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**Próximo paso:** Aplicar proceso a restructure-multitenant RFC
