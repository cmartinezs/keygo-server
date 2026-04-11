# Deprecated & Archived Documentation

These documents are no longer maintained and have been superseded by newer canonical documentation.

**Last updated:** 2026-04-10  
**Total archived:** 14 documents

---

## Archived by Category

### Design Documents (7 files in `design/`)

| Document | Reason | See Instead |
|---|---|---|
| `BACKLOG.md` | Outdated roadmap (pre-Sprint structure) | [`../../ROADMAP.md`](../../ROADMAP.md) |
| `IMPLEMENTATION_PLAN.md` | Superseded by RFCs and Sprints | [`../../rfc/`](../../rfc/) + Sprint docs |
| `PROJECT_STRUCTURE.md` | Covered in ARCHITECTURE.md | [`../../design/ARCHITECTURE.md`](../../design/ARCHITECTURE.md) |
| `DOMAIN_MODEL.md` | Covered in PATTERNS.md | [`../../design/patterns/PATTERNS.md`](../../design/patterns/PATTERNS.md) |
| `EXCEPTION_HIERARCHY.md` | Part of ERROR_CATALOG.md | [`../../design/api/ERROR_CATALOG.md`](../../design/api/ERROR_CATALOG.md) |
| `UI_CONFIGURATION.md` | Not actively used | [`../../development/ENVIRONMENT_SETUP.md`](../../development/ENVIRONMENT_SETUP.md) |
| `I18N_STRATEGY.md` | Incomplete proposal | [`../../design/RFC_CLOSURE_PROCESS.md`](../../design/RFC_CLOSURE_PROCESS.md) |
| `TRACING_TELEMETRY.md` | Consolidated into OBSERVABILITY | [`../../design/OBSERVABILITY.md`](../../design/OBSERVABILITY.md) |
| `API_SURFACE.md` | Consolidated into ENDPOINT_CATALOG | [`../../design/api/ENDPOINT_CATALOG.md`](../../design/api/ENDPOINT_CATALOG.md) |

### Data Documents (2 files in `data/`)

| Document | Reason | See Instead |
|---|---|---|
| `DATA_MODEL.md` | Consolidated into DATABASE_SCHEMA | [`../../design/DATABASE_SCHEMA.md`](../../design/DATABASE_SCHEMA.md) |
| `ENTITY_RELATIONSHIPS.md` | Consolidated into DATABASE_SCHEMA | [`../../design/DATABASE_SCHEMA.md#entity-relationship-diagram-erd`](../../design/DATABASE_SCHEMA.md#entity-relationship-diagram-erd) |

### API Documents (1 file in `api/`)

| Document | Reason | See Instead |
|---|---|---|
| `BOOTSTRAP_FILTER.md` | Implementation detail, not design | Code comments in `keygo-run/filter/` |

### Development Documents (1 file in `development/`)

| Document | Reason | See Instead |
|---|---|---|
| `INTELLIJ_SETUP.md` | IDE-specific, low priority | [`../../development/ENVIRONMENT_SETUP.md`](../../development/ENVIRONMENT_SETUP.md) |

### Frontend Documents (1 file in `keygo-ui/`)

| Document | Reason | See Instead |
|---|---|---|
| `FRONTEND_DEVELOPER_GUIDE.md` | Moved to main development docs | [`../../development/FRONTEND_DEVELOPER_GUIDE.md`](../../development/FRONTEND_DEVELOPER_GUIDE.md) |

---

## How to Restore

If you need to access an archived document:
1. Check the "See Instead" column for current documentation
2. Search git history: `git log --all --oneline -- docs/design/BACKLOG.md`
3. View at specific commit: `git show <commit>:docs/design/BACKLOG.md`

---

## Archival Policy

Documents are archived when:
- Superseded by newer canonical documentation
- No longer relevant to current architecture
- Implementation details better kept in code comments
- Incomplete or outdated beyond practical use

Archived documents remain in git history for reference but are not maintained.

---

**Note:** If you believe a document should be restored or updated, please check the current [`../../README.md`](../../README.md) for the canonical version.
