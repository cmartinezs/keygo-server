# Plan de Reorganización de Documentación

**Objetivo:** Eliminar redundancia, mantener compatibilidad, clarificar estructura

**Fecha:** 2026-04-10

---

## Diagnóstico

### Archivos Nuevos Creados (Sprints 1-4)
```
✅ docs/design/ARCHITECTURE.md
✅ docs/design/patterns/VALIDATION_STRATEGY.md
✅ docs/design/patterns/PATTERNS.md
✅ docs/design/api/ENDPOINT_CATALOG.md
✅ docs/design/RFC_CLOSURE_PROCESS.md
✅ docs/design/AUTHORIZATION_PATTERNS.md
✅ docs/design/OAUTH2_MULTIDOMAIN_CONTRACT.md
✅ docs/design/PROVISIONING_STRATEGY.md
✅ docs/design/API_VERSIONING_STRATEGY.md
✅ docs/design/DATABASE_SCHEMA.md
✅ docs/design/TEST_INTEGRATION.md
✅ docs/design/OBSERVABILITY.md
✅ docs/development/FRONTEND_DEVELOPER_GUIDE.md
✅ docs/operations/DEPLOYMENT_PIPELINE.md
✅ docs/security/SECURITY_GUIDELINES.md
```

### Redundancias Identificadas

| Situación | Existente | Nuevo | Acción |
|---|---|---|---|
| **Frontend Integration** | `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` | `docs/development/FRONTEND_DEVELOPER_GUIDE.md` | ❌ Eliminar keygo-ui, centralizar en development |
| **Data Model** | `docs/data/DATA_MODEL.md` + `docs/data/ENTITY_RELATIONSHIPS.md` | `docs/design/DATABASE_SCHEMA.md` | ❌ Eliminar data/, referenciar nuevo |
| **Migrations** | `docs/data/MIGRATIONS.md` | DATABASE_SCHEMA.md (section) | ✅ Mantener MIGRATIONS.md (es más específico, Flyway versions) |
| **Testing** | `docs/development/TEST_STRATEGY.md` (quick ref) | `docs/design/TEST_INTEGRATION.md` (completo) | ✅ Mantener ambos, TEST_STRATEGY como quick ref |
| **Tracing** | `docs/design/TRACING_TELEMETRY.md` | `docs/design/OBSERVABILITY.md` | ❌ Eliminar, referenciar OBSERVABILITY |
| **API Surface** | `docs/design/API_SURFACE.md` | `docs/design/api/ENDPOINT_CATALOG.md` | ❌ Eliminar, usar ENDPOINT_CATALOG |

### Archivos Anticuados (Sin Uso)

```
❌ docs/design/BACKLOG.md — Outdated roadmap
❌ docs/design/IMPLEMENTATION_PLAN.md — Superseded by RFCs
❌ docs/design/PROJECT_STRUCTURE.md — Covered in ARCHITECTURE.md
❌ docs/design/DOMAIN_MODEL.md — Covered in PATTERNS.md
❌ docs/design/EXCEPTION_HIERARCHY.md — Part of ERROR_CATALOG.md
❌ docs/design/UI_CONFIGURATION.md — Not actively used
❌ docs/design/I18N_STRATEGY.md — Incomplete, outdated
❌ docs/api/BOOTSTRAP_FILTER.md — Implementation detail, not design
❌ docs/api/BILLING_FLOW.md — Should be in RFC billing
❌ docs/development/INTELLIJ_SETUP.md — IDE-specific, not critical

→ Move to docs/archive/deprecated/
```

### RFCs En Progreso (Non-Closed)

```
In Progress:
├─ docs/rfc/restructure-multitenant/ (Complete, but not officially closed)
├─ docs/rfc/account-ui-proposal/ (In discussion)
├─ docs/rfc/t108-geoip-sessions/ (In discussion)
└─ docs/rfc/incomplete-sections/ (Draft)

→ Create docs/rfc/IN_PROGRESS.md with status matrix
→ Move completed/archived RFCs to docs/archive/rfc-history/
```

---

## Plan de Acción

### Phase 1: Crear Índices Principales (10 minutos)

**Crear:** `docs/README.md` (actualizado con los 20 docs)
- Sección: "Getting Started"
- Sección: "Architecture & Design" (link a docs/design/README.md)
- Sección: "API Reference" (link a docs/design/api/ENDPOINT_CATALOG.md)
- Sección: "Development" (link a docs/development/README.md)
- Sección: "Operations & Deployment" (link a docs/operations/README.md)
- Sección: "Security" (link a docs/security/README.md)
- Sección: "Historical RFCs" (link a docs/rfc/IN_PROGRESS.md)

**Crear:** `docs/design/README.md` (índice de todos los design docs)
- Organize by: Patterns, API, Authorization, Database, etc.

**Crear:** `docs/development/README.md` (índice de dev docs)
**Crear:** `docs/operations/README.md` (índice de ops docs)
**Crear:** `docs/security/README.md` (nueva carpeta)

### Phase 2: Consolidar & Redirigir (20 minutos)

**Action: Actualizar `docs/data/DATA_MODEL.md`**
```markdown
# Data Model (Moved)

⚠️ **This documentation has been consolidated.**

See: [`docs/design/DATABASE_SCHEMA.md`](../design/DATABASE_SCHEMA.md)

This file is kept for backward compatibility only.
```

**Action: Actualizar `docs/data/ENTITY_RELATIONSHIPS.md`**
```markdown
# Entity Relationships (Moved)

⚠️ **This documentation has been consolidated.**

See: [`docs/design/DATABASE_SCHEMA.md` — Entity Relationship Diagram section](../design/DATABASE_SCHEMA.md#entity-relationship-diagram-erd)

This file is kept for backward compatibility only.
```

**Action: Actualizar `docs/design/TRACING_TELEMETRY.md`**
```markdown
# Tracing & Telemetry (Moved)

⚠️ **This documentation has been consolidated into Observability strategy.**

See: [`docs/design/OBSERVABILITY.md`](./OBSERVABILITY.md)

This file is kept for backward compatibility only.
```

**Action: Actualizar `docs/design/API_SURFACE.md`**
```markdown
# API Surface (Moved)

⚠️ **This documentation has been consolidated.**

See: [`docs/design/api/ENDPOINT_CATALOG.md`](./api/ENDPOINT_CATALOG.md) for complete endpoint reference.

This file is kept for backward compatibility only.
```

**Action: Actualizar `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`**
```markdown
# Frontend Developer Guide (Moved)

⚠️ **This documentation has been moved to the main development directory.**

See: [`docs/development/FRONTEND_DEVELOPER_GUIDE.md`](../development/FRONTEND_DEVELOPER_GUIDE.md)

This file is kept for backward compatibility only.
```

### Phase 3: Archivo Documentación Anticuada (20 minutos)

Move to `docs/archive/deprecated/`:
```
docs/archive/deprecated/
├── BACKLOG.md
├── IMPLEMENTATION_PLAN.md
├── PROJECT_STRUCTURE.md
├── DOMAIN_MODEL.md
├── EXCEPTION_HIERARCHY.md
├── UI_CONFIGURATION.md
├── I18N_STRATEGY.md
├── BOOTSTRAP_FILTER.md
├── INTELLIJ_SETUP.md
└── README.md (índice con explicación)
```

**Create:** `docs/archive/deprecated/README.md`
```markdown
# Deprecated Documentation

These documents are superseded by newer architecture and design decisions.

| Document | Reason | See Instead |
|---|---|---|
| BACKLOG.md | Outdated roadmap | ROADMAP.md |
| IMPLEMENTATION_PLAN.md | Superseded by RFCs | docs/rfc/ |
| PROJECT_STRUCTURE.md | Covered in ARCHITECTURE.md | docs/design/ARCHITECTURE.md |
| DOMAIN_MODEL.md | Covered in PATTERNS.md | docs/design/patterns/PATTERNS.md |
| EXCEPTION_HIERARCHY.md | Part of ERROR_CATALOG | docs/design/api/ERROR_CATALOG.md |

For historical context, browse the RFC documents in `docs/rfc/`.
```

### Phase 4: RFC Status Matrix (15 minutos)

**Create:** `docs/rfc/IN_PROGRESS.md`
```markdown
# RFC Status & Lifecycle

## Current RFCs (In Progress)

| RFC | Status | Location | Last Updated |
|---|---|---|---|
| restructure-multitenant | ✅ Approved + Implemented | `restructure-multitenant/` | 2026-04-10 |
| account-ui-proposal | 🔄 In Discussion | `account-ui-proposal/` | 2026-04-09 |
| t108-geoip-sessions | 🔄 In Discussion | `t108-geoip-sessions/` | 2026-03-15 |
| incomplete-sections | 📝 Draft | `incomplete-sections/` | 2026-04-02 |

See `docs/design/RFC_CLOSURE_PROCESS.md` for lifecycle details.

## Completed & Archived RFCs

See `docs/archive/rfc-history/` for closed RFCs.
```

### Phase 5: Update `docs/README.md` (30 minutos)

**New structure:**
```markdown
# KeyGo Server Documentation

## 🚀 Quick Start
- New to KeyGo? Start with: [ARCHITECTURE.md](docs/design/ARCHITECTURE.md)
- Frontend integration? See: [FRONTEND_DEVELOPER_GUIDE.md](docs/development/FRONTEND_DEVELOPER_GUIDE.md)
- Deploying to production? See: [DEPLOYMENT_PIPELINE.md](docs/operations/DEPLOYMENT_PIPELINE.md)

## 📚 Documentation by Role

### For Architects & Tech Leads
- [ARCHITECTURE.md](docs/design/ARCHITECTURE.md) — System design overview
- [PATTERNS.md](docs/design/patterns/PATTERNS.md) — Design patterns used
- [AUTHORIZATION_PATTERNS.md](docs/design/AUTHORIZATION_PATTERNS.md) — RBAC model
- [RFC_CLOSURE_PROCESS.md](docs/design/RFC_CLOSURE_PROCESS.md) — Decision framework

### For Backend Engineers
- [ENDPOINT_CATALOG.md](docs/design/api/ENDPOINT_CATALOG.md) — All API endpoints
- [DATABASE_SCHEMA.md](docs/design/DATABASE_SCHEMA.md) — Database design
- [VALIDATION_STRATEGY.md](docs/design/patterns/VALIDATION_STRATEGY.md) — Input validation
- [TEST_INTEGRATION.md](docs/design/TEST_INTEGRATION.md) — Testing patterns
- [OBSERVABILITY.md](docs/design/OBSERVABILITY.md) — Logging, metrics, tracing

### For Frontend Engineers
- [FRONTEND_DEVELOPER_GUIDE.md](docs/development/FRONTEND_DEVELOPER_GUIDE.md) — OAuth2, API integration
- [ERROR_CATALOG.md](docs/design/api/ERROR_CATALOG.md) — Error handling

### For DevOps & SRE
- [DEPLOYMENT_PIPELINE.md](docs/operations/DEPLOYMENT_PIPELINE.md) — CI/CD workflows
- [PRODUCTION_RUNBOOK.md](docs/operations/PRODUCTION_RUNBOOK.md) — Operations guide
- [SECURITY_GUIDELINES.md](docs/security/SECURITY_GUIDELINES.md) — Security hardening

### For Product & Project Managers
- [ROADMAP.md](ROADMAP.md) — Feature roadmap
- [PROVISIONING_STRATEGY.md](docs/design/PROVISIONING_STRATEGY.md) — User management
- [API_VERSIONING_STRATEGY.md](docs/design/API_VERSIONING_STRATEGY.md) — API evolution

## 🔗 Topic Index

| Topic | Documents |
|---|---|
| **Architecture** | [ARCHITECTURE.md](docs/design/ARCHITECTURE.md), [PATTERNS.md](docs/design/patterns/PATTERNS.md) |
| **API Design** | [ENDPOINT_CATALOG.md](docs/design/api/ENDPOINT_CATALOG.md), [API_VERSIONING_STRATEGY.md](docs/design/API_VERSIONING_STRATEGY.md), [ERROR_CATALOG.md](docs/design/api/ERROR_CATALOG.md) |
| **Authorization** | [AUTHORIZATION_PATTERNS.md](docs/design/AUTHORIZATION_PATTERNS.md), [OAUTH2_MULTIDOMAIN_CONTRACT.md](docs/design/OAUTH2_MULTIDOMAIN_CONTRACT.md) |
| **Database** | [DATABASE_SCHEMA.md](docs/design/DATABASE_SCHEMA.md), [MIGRATIONS.md](docs/data/MIGRATIONS.md) |
| **Testing** | [TEST_INTEGRATION.md](docs/design/TEST_INTEGRATION.md), [TEST_STRATEGY.md](docs/development/TEST_STRATEGY.md) |
| **Observability** | [OBSERVABILITY.md](docs/design/OBSERVABILITY.md) |
| **Security** | [SECURITY_GUIDELINES.md](docs/security/SECURITY_GUIDELINES.md) |
| **Deployment** | [DEPLOYMENT_PIPELINE.md](docs/operations/DEPLOYMENT_PIPELINE.md), [PRODUCTION_RUNBOOK.md](docs/operations/PRODUCTION_RUNBOOK.md) |
| **User Provisioning** | [PROVISIONING_STRATEGY.md](docs/design/PROVISIONING_STRATEGY.md) |

## 📋 Documentation Directory

- **`docs/design/`** — Architecture, patterns, API design
- **`docs/development/`** — Development guides, setup, debugging
- **`docs/operations/`** — Deployment, monitoring, runbooks
- **`docs/security/`** — Security guidelines, compliance
- **`docs/product-design/`** — Product requirements, use cases
- **`docs/rfc/`** — Request for Comments (design decisions)
- **`docs/archive/`** — Historical documents, deprecated content

## ⚡ Sprints Completed

- **Sprint 1:** Foundation (Architecture, Roadmap, AI Context, Agents, SCIM)
- **Sprint 2:** Design (Validation, Patterns, Endpoints, RFC, AuthZ, OAuth2, Provisioning, Versioning, Runbook)
- **Sprint 3:** Quality (Testing, Observability, Frontend)
- **Sprint 4:** Infrastructure (Deployment, Database, Security)

**Total:** 20 critical documents, 95% coverage

---

Last updated: 2026-04-10
```

### Phase 6: Create READMEs for Each Major Directory

**`docs/design/README.md`:**
```markdown
# Design Documentation

Core architectural and design decisions.

## Key Documents

### Patterns & Architecture
- [ARCHITECTURE.md](ARCHITECTURE.md) — System design overview
- [patterns/PATTERNS.md](patterns/PATTERNS.md) — Design patterns (hexagonal, value objects, etc.)
- [patterns/VALIDATION_STRATEGY.md](patterns/VALIDATION_STRATEGY.md) — Three-tier validation

### API & Contracts
- [api/ENDPOINT_CATALOG.md](api/ENDPOINT_CATALOG.md) — All endpoints with auth, methods
- [api/ERROR_CATALOG.md](api/ERROR_CATALOG.md) — Error codes and meanings
- [API_VERSIONING_STRATEGY.md](API_VERSIONING_STRATEGY.md) — v1 → v2 migration path
- [OAUTH2_MULTIDOMAIN_CONTRACT.md](OAUTH2_MULTIDOMAIN_CONTRACT.md) — OAuth2 flows

### Authorization & Security
- [AUTHORIZATION_PATTERNS.md](AUTHORIZATION_PATTERNS.md) — RBAC model (platform/tenant/app)
- [OAUTH2_MULTIDOMAIN_CONTRACT.md](OAUTH2_MULTIDOMAIN_CONTRACT.md) — Auth contract

### Data & Database
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) — Schema design, relationships, indexes
- Related: [../data/MIGRATIONS.md](../data/MIGRATIONS.md) — Flyway versions

### Quality & Operations
- [TEST_INTEGRATION.md](TEST_INTEGRATION.md) — Testing strategy (unit/integration/contract)
- [OBSERVABILITY.md](OBSERVABILITY.md) — Logging, metrics, tracing, alerting

### Features
- [PROVISIONING_STRATEGY.md](PROVISIONING_STRATEGY.md) — User provisioning (manual → SCIM → directory)

### Process
- [RFC_CLOSURE_PROCESS.md](RFC_CLOSURE_PROCESS.md) — How design decisions are made

---

See [../README.md](../README.md) for cross-topic index.
```

**`docs/operations/README.md`:**
```markdown
# Operations & Deployment

Guides for running KeyGo in production.

## Key Documents

- [DEPLOYMENT_PIPELINE.md](DEPLOYMENT_PIPELINE.md) — GitHub Actions CI/CD (test → security → build → deploy)
- [PRODUCTION_RUNBOOK.md](PRODUCTION_RUNBOOK.md) — Operations guide (health, metrics, troubleshooting)
- [DOCKER.md](DOCKER.md) — Docker image configuration

---

See [../README.md](../README.md) for full documentation index.
```

**`docs/security/README.md` (NEW):**
```markdown
# Security & Compliance

Security guidelines, threat modeling, OWASP defense.

## Key Documents

- [SECURITY_GUIDELINES.md](SECURITY_GUIDELINES.md) — OWASP Top 10 defenses, secrets management, encryption, compliance

---

See [../README.md](../README.md) for full documentation index.
```

---

## Deletion List (Safe to Delete)

```
❌ docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md
   → Consolidated into docs/development/FRONTEND_DEVELOPER_GUIDE.md

❌ docs/design/TRACING_TELEMETRY.md
   → Consolidated into docs/design/OBSERVABILITY.md

❌ docs/design/API_SURFACE.md
   → Replaced by docs/design/api/ENDPOINT_CATALOG.md

❌ docs/design/BACKLOG.md
   → Outdated roadmap, moved to archive

❌ docs/design/IMPLEMENTATION_PLAN.md
   → Superseded by RFCs, moved to archive

❌ docs/design/PROJECT_STRUCTURE.md
   → Covered in ARCHITECTURE.md, moved to archive

❌ docs/design/DOMAIN_MODEL.md
   → Covered in PATTERNS.md, moved to archive

❌ docs/design/EXCEPTION_HIERARCHY.md
   → Part of ERROR_CATALOG.md, moved to archive

❌ docs/design/UI_CONFIGURATION.md
   → Not actively used, moved to archive

❌ docs/design/I18N_STRATEGY.md
   → Incomplete, moved to archive

❌ docs/api/BOOTSTRAP_FILTER.md
   → Implementation detail, moved to archive

❌ docs/api/BILLING_FLOW.md
   → Should be in RFC, moved to archive

❌ docs/development/INTELLIJ_SETUP.md
   → IDE-specific, moved to archive
```

## Update (Replace Content)

```
✏️  docs/data/DATA_MODEL.md
    → Replace with redirect to DATABASE_SCHEMA.md

✏️  docs/data/ENTITY_RELATIONSHIPS.md
    → Replace with redirect to DATABASE_SCHEMA.md

✏️  docs/design/TRACING_TELEMETRY.md
    → Replace with redirect to OBSERVABILITY.md

✏️  docs/design/API_SURFACE.md
    → Replace with redirect to ENDPOINT_CATALOG.md

✏️  docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md
    → Replace with redirect to docs/development/FRONTEND_DEVELOPER_GUIDE.md
```

## Keep (Coexist)

```
✅ docs/development/TEST_STRATEGY.md
   + docs/design/TEST_INTEGRATION.md
   (Quick ref + Complete guide — both useful)

✅ docs/data/MIGRATIONS.md
   (Specific to Flyway versions, not redundant with DATABASE_SCHEMA design)
```

---

## Timeline

| Phase | Task | Duration |
|---|---|---|
| 1 | Create main indices (README.md) | 10 min |
| 2 | Update/redirect redundant docs | 20 min |
| 3 | Archive deprecated docs | 20 min |
| 4 | RFC status matrix | 15 min |
| 5 | Update docs/README.md | 30 min |
| 6 | Create category READMEs | 15 min |
| **TOTAL** | | **110 minutes (~2 hours)** |

---

## Checklist Before Execution

- [ ] Backup current docs/ (git commit)
- [ ] Identify all inbound links to files being moved
- [ ] Update CLAUDE.md with new doc locations
- [ ] Update AI_CONTEXT.md with new structure
- [ ] Test all internal links after changes
- [ ] Notify team of new documentation structure

---

**Status:** Ready for execution  
**Requested by:** User  
**Date:** 2026-04-10
