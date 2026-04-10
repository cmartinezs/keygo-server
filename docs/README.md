# KeyGo Server Documentation

Complete reference for architecture, design, development, and operations.

---

## 🚀 Quick Start

**New to KeyGo?**
- Start here: [`ARCHITECTURE.md`](design/ARCHITECTURE.md) — System design overview
- Then read: [`ROADMAP.md`](../ROADMAP.md) — Feature roadmap and timeline

**Frontend integration?**
- See: [`FRONTEND_DEVELOPER_GUIDE.md`](development/FRONTEND_DEVELOPER_GUIDE.md) — OAuth2, API integration, testing

**Deploying to production?**
- See: [`DEPLOYMENT_PIPELINE.md`](operations/DEPLOYMENT_PIPELINE.md) — CI/CD with GitHub Actions
- Then: [`PRODUCTION_RUNBOOK.md`](operations/PRODUCTION_RUNBOOK.md) — Operations guide

---

## 📚 Documentation by Role

### Architects & Tech Leads
- [`ARCHITECTURE.md`](design/ARCHITECTURE.md) — System design overview, layers, components
- [`design/patterns/PATTERNS.md`](design/patterns/PATTERNS.md) — Hexagonal architecture, domain-driven design, common patterns
- [`AUTHORIZATION_PATTERNS.md`](design/AUTHORIZATION_PATTERNS.md) — Platform/Tenant/App RBAC model
- [`OAUTH2_MULTIDOMAIN_CONTRACT.md`](design/OAUTH2_MULTIDOMAIN_CONTRACT.md) — OAuth2 authentication contract
- [`RFC_CLOSURE_PROCESS.md`](design/RFC_CLOSURE_PROCESS.md) — Decision framework and lifecycle
- [`rfc/`](rfc/) — Historical design decisions and RFCs

### Backend Engineers
- [`design/api/ENDPOINT_CATALOG.md`](design/api/ENDPOINT_CATALOG.md) — Complete API endpoint reference (~60 endpoints)
- [`DATABASE_SCHEMA.md`](design/DATABASE_SCHEMA.md) — Database design, relationships, migrations
- [`design/patterns/VALIDATION_STRATEGY.md`](design/patterns/VALIDATION_STRATEGY.md) — Input validation (3 tiers: HTTP/DTO, Domain, UseCase)
- [`TEST_INTEGRATION.md`](design/TEST_INTEGRATION.md) — Testing strategy (unit/integration/contract with examples)
- [`OBSERVABILITY.md`](design/OBSERVABILITY.md) — Logging, metrics, distributed tracing, alerts
- [`data/MIGRATIONS.md`](data/MIGRATIONS.md) — Flyway migration versions (V1-V17)

### Frontend Engineers
- [`FRONTEND_DEVELOPER_GUIDE.md`](development/FRONTEND_DEVELOPER_GUIDE.md) — OAuth2 PKCE, API integration, React patterns, error handling
- [`design/api/ERROR_CATALOG.md`](design/api/ERROR_CATALOG.md) — Error codes and what to show users
- [`AUTHORIZATION_PATTERNS.md`](design/AUTHORIZATION_PATTERNS.md) — Understanding JWT tokens and scopes

### DevOps & SRE
- [`DEPLOYMENT_PIPELINE.md`](operations/DEPLOYMENT_PIPELINE.md) — GitHub Actions CI/CD (3 environments: dev/staging/prod)
- [`PRODUCTION_RUNBOOK.md`](operations/PRODUCTION_RUNBOOK.md) — Operations guide, health checks, troubleshooting
- [`SECURITY_GUIDELINES.md`](security/SECURITY_GUIDELINES.md) — OWASP Top 10 defenses, secrets management, compliance
- [`operations/DOCKER.md`](operations/DOCKER.md) — Docker image configuration

### Product & Project Managers
- [`product/README.md`](product/README.md) — Product context, requirements, and business flows
- [`ROADMAP.md`](../ROADMAP.md) — Feature roadmap and timeline
- [`PROVISIONING_STRATEGY.md`](design/PROVISIONING_STRATEGY.md) — User provisioning (manual → SCIM → directory integration)
- [`API_VERSIONING_STRATEGY.md`](design/API_VERSIONING_STRATEGY.md) — API evolution and deprecation lifecycle

---

## 🔗 Documentation by Topic

| Topic | Documents |
|---|---|
| **Architecture** | [`ARCHITECTURE.md`](design/ARCHITECTURE.md), [`design/patterns/PATTERNS.md`](design/patterns/PATTERNS.md) |
| **API Design** | [`design/api/ENDPOINT_CATALOG.md`](design/api/ENDPOINT_CATALOG.md), [`API_VERSIONING_STRATEGY.md`](design/API_VERSIONING_STRATEGY.md), [`design/api/ERROR_CATALOG.md`](design/api/ERROR_CATALOG.md) |
| **Authorization** | [`AUTHORIZATION_PATTERNS.md`](design/AUTHORIZATION_PATTERNS.md), [`OAUTH2_MULTIDOMAIN_CONTRACT.md`](design/OAUTH2_MULTIDOMAIN_CONTRACT.md) |
| **Database** | [`DATABASE_SCHEMA.md`](design/DATABASE_SCHEMA.md), [`data/MIGRATIONS.md`](data/MIGRATIONS.md) |
| **Testing** | [`TEST_INTEGRATION.md`](design/TEST_INTEGRATION.md), [`development/TEST_STRATEGY.md`](development/TEST_STRATEGY.md) |
| **Observability** | [`OBSERVABILITY.md`](design/OBSERVABILITY.md) |
| **Security** | [`SECURITY_GUIDELINES.md`](security/SECURITY_GUIDELINES.md) |
| **Deployment** | [`DEPLOYMENT_PIPELINE.md`](operations/DEPLOYMENT_PIPELINE.md), [`PRODUCTION_RUNBOOK.md`](operations/PRODUCTION_RUNBOOK.md) |
| **Product Context** | [`product/README.md`](product/README.md), [`product/BOUNDED_CONTEXTS.md`](product/BOUNDED_CONTEXTS.md) |
| **Provisioning** | [`PROVISIONING_STRATEGY.md`](design/PROVISIONING_STRATEGY.md) |
| **Validation** | [`design/patterns/VALIDATION_STRATEGY.md`](design/patterns/VALIDATION_STRATEGY.md) |
| **RFCs & Decisions** | [`rfc/README.md`](rfc/README.md), [`rfc/IN_PROGRESS.md`](rfc/IN_PROGRESS.md) |

---

## 📂 Documentation Structure

```
docs/
├── design/                          ← Architecture & Design
│   ├── ARCHITECTURE.md
│   ├── AUTHORIZATION_PATTERNS.md
│   ├── OAUTH2_MULTIDOMAIN_CONTRACT.md
│   ├── API_VERSIONING_STRATEGY.md
│   ├── DATABASE_SCHEMA.md
│   ├── PROVISIONING_STRATEGY.md
│   ├── OBSERVABILITY.md
│   ├── TEST_INTEGRATION.md
│   ├── RFC_CLOSURE_PROCESS.md
│   ├── api/
│   │   ├── ENDPOINT_CATALOG.md      ← ~60 endpoints
│   │   └── ERROR_CATALOG.md
│   └── patterns/
│       ├── PATTERNS.md
│       └── VALIDATION_STRATEGY.md
├── development/                      ← Development Guides
│   ├── README.md
│   ├── FRONTEND_DEVELOPER_GUIDE.md
│   ├── TEST_STRATEGY.md
│   ├── CODE_STYLE.md
│   ├── ENVIRONMENT_SETUP.md
│   └── DEBUG_GUIDE.md
├── operations/                       ← Deployment & Operations
│   ├── README.md
│   ├── DEPLOYMENT_PIPELINE.md
│   ├── PRODUCTION_RUNBOOK.md
│   └── DOCKER.md
├── security/                         ← Security & Compliance
│   ├── README.md
│   └── SECURITY_GUIDELINES.md
├── product/                          ← Product Requirements & Context
│   ├── README.md
│   ├── REQUERIMIENTOS.md
│   ├── BOUNDED_CONTEXTS.md
│   ├── PROPUESTA_SOLUCION.md
│   ├── DIAGRAMAS/
│   └── flows/
├── rfc/                              ← Design Decisions & RFCs
│   ├── README.md
│   ├── IN_PROGRESS.md
│   ├── restructure-multitenant/
│   ├── restructure-implementation/
│   ├── billing-contractor-refactor/
│   ├── account-ui-proposal/
│   ├── t108-geoip-sessions/
│   └── incomplete-sections/
├── data/                             ← Database
│   └── MIGRATIONS.md
├── plans/                            ← Implementation Plans
│   └── documentacion-2026/
├── ai/                               ← AI Agent Operations (Internal)
│   ├── AGENT_OPERATIONS.md
│   ├── lecciones/
│   ├── propuestas/
│   └── agents-registro/
└── archive/                          ← Historical & Deprecated
    ├── deprecated/
    ├── email-templates/
    ├── rfc-history/
    └── research/
```

---

## 📊 Documentation Coverage

| Area | Status | Documents |
|---|---|---|
| **Architecture** | ✅ Complete | ARCHITECTURE.md, PATTERNS.md |
| **API Design** | ✅ Complete | ENDPOINT_CATALOG.md, API_VERSIONING.md, ERROR_CATALOG.md |
| **Authorization** | ✅ Complete | AUTHORIZATION_PATTERNS.md, OAUTH2_CONTRACT.md |
| **Database** | ✅ Complete | DATABASE_SCHEMA.md, MIGRATIONS.md |
| **Testing** | ✅ Complete | TEST_INTEGRATION.md, TEST_STRATEGY.md |
| **Observability** | ✅ Complete | OBSERVABILITY.md |
| **Security** | ✅ Complete | SECURITY_GUIDELINES.md |
| **Deployment** | ✅ Complete | DEPLOYMENT_PIPELINE.md, PRODUCTION_RUNBOOK.md |
| **Frontend** | ✅ Complete | FRONTEND_DEVELOPER_GUIDE.md |
| **Provisioning** | ✅ Complete | PROVISIONING_STRATEGY.md |

**Total:** 20 critical documents (95% coverage)

---

## 🔍 Documentation Organization

**Main Sections (for users):**
- `design/` — Canonical architectural decisions
- `development/` — Developer guides and setup
- `operations/` — Deployment and operations
- `security/` — Security requirements and compliance
- `product/` — Product context, requirements, business flows

**Decision Tracking:**
- `rfc/README.md` — All RFCs in one central location (6 active)
- `rfc/IN_PROGRESS.md` — Current status of architecture decisions
- `design/RFC_CLOSURE_PROCESS.md` — How decisions are made and archived

**Internal Operations (not user docs):**
- `ai/` — AI agent operations, memory, and lessons learned
- `plans/` — Implementation planning and sprints

---

## 🎯 Sprints Completed

- **Sprint 1:** Foundation (Architecture, Roadmap, AI Context, Agents)
- **Sprint 2:** Design (Validation, Patterns, Endpoints, RFC Closure, AuthZ, OAuth2, Provisioning, Versioning, Runbook)
- **Sprint 3:** Quality (Testing, Observability, Frontend)
- **Sprint 4:** Infrastructure (Deployment, Database, Security)
- **Phase 5:** Documentation Reorganization (2026-04-09) — Consolidated RFCs, reorganized product docs, archived email templates

---

## ⚡ For Specific Tasks

### "How do I set up my development environment?"
→ [`development/ENVIRONMENT_SETUP.md`](development/ENVIRONMENT_SETUP.md)

### "What's the current API status?"
→ [`design/api/ENDPOINT_CATALOG.md`](design/api/ENDPOINT_CATALOG.md)

### "How do I add a new endpoint?"
→ [`design/api/ENDPOINT_CATALOG.md`](design/api/ENDPOINT_CATALOG.md) + [`development/CODE_STYLE.md`](development/CODE_STYLE.md)

### "How should I authenticate?"
→ [`FRONTEND_DEVELOPER_GUIDE.md`](development/FRONTEND_DEVELOPER_GUIDE.md) (frontend) or [`OAUTH2_MULTIDOMAIN_CONTRACT.md`](design/OAUTH2_MULTIDOMAIN_CONTRACT.md) (backend)

### "What's the RBAC model?"
→ [`AUTHORIZATION_PATTERNS.md`](design/AUTHORIZATION_PATTERNS.md)

### "How do I deploy to production?"
→ [`DEPLOYMENT_PIPELINE.md`](operations/DEPLOYMENT_PIPELINE.md) then [`PRODUCTION_RUNBOOK.md`](operations/PRODUCTION_RUNBOOK.md)

### "What are the security requirements?"
→ [`SECURITY_GUIDELINES.md`](security/SECURITY_GUIDELINES.md)

### "How do I test my code?"
→ [`TEST_INTEGRATION.md`](design/TEST_INTEGRATION.md)

### "What error codes exist?"
→ [`design/api/ERROR_CATALOG.md`](design/api/ERROR_CATALOG.md)

---

## 🔍 Search Tips

- **By document name:** `docs/design/` for design, `docs/development/` for dev, `docs/operations/` for ops
- **By topic:** See "Documentation by Topic" section above
- **By role:** See "Documentation by Role" section above
- **All endpoints:** [`design/api/ENDPOINT_CATALOG.md`](design/api/ENDPOINT_CATALOG.md)
- **Error codes:** [`design/api/ERROR_CATALOG.md`](design/api/ERROR_CATALOG.md)

---

## 📞 Getting Help

- **Technical questions?** Check the relevant documentation above
- **Need to propose a change?** See [`design/RFC_CLOSURE_PROCESS.md`](design/RFC_CLOSURE_PROCESS.md)
- **Found an error?** File an issue with doc location
- **Documentation unclear?** Suggest improvements

---

**Last updated:** 2026-04-10  
**Total docs:** 20 critical documents  
**Coverage:** 95% of architecture, operations, and security
