# Master Reorganization Plan — Complete Documentation Structure

**Date:** 2026-04-10  
**Status:** Analysis Complete, Ready for Implementation  
**Scope:** Complete cleanup of docs/ folder

---

## Current State Analysis

### Problems Identified

1. **RFCs in 4 different locations** ❌
   - `docs/rfc/` (26 files - main)
   - `docs/design/rfc-billing-contractor-refactor/` (scattered)
   - `docs/design/rfc-restructure-implementation/` (scattered)
   - `docs/archive/rfc/` (historical)
   - **Solution:** Consolidate all into `docs/rfc/`, archive old ones in `docs/archive/rfc-history/`

2. **Multiple README.md files in same directories** ❌
   - `docs/design/` has 9 README.md files (same name!)
   - `docs/development/` has 4 README.md files
   - `docs/rfc/` has 3 README.md files
   - **Solution:** Keep only ONE README.md per folder, delete duplicates

3. **Empty or near-empty folders** ❌
   - `docs/postman/` (0 .md files - infrastructure, not docs)
   - `docs/scripts/` (0 .md files - tools, not docs)
   - `docs/sql/` (0 .md files - queries, not docs)
   - `docs/plans/` (only README)
   - `docs/keygo-ui/` (1 file - should be gone)
   - **Solution:** Move to root `.gitignore` or `/scripts`, `/sql` directories, delete empty folders from docs/

4. **Orphaned docs in docs/api/** ❌
   - `AUTH_FLOW.md`, `BILLING_FLOW.md`, `OPENAPI.md`, `RESPONSE_CODES.md`
   - These should be in `docs/design/api/` or consolidated
   - **Solution:** Move to `docs/design/api/` or archive if redundant

5. **Email templates taking up space** ⚠️
   - 11 files in `docs/design/email/`
   - These are specific to a feature but not core architecture
   - **Solution:** Move to `docs/archive/email-templates/` (recoverable but not in main navigation)

6. **Product-design separated from design** ⚠️
   - `docs/product-design/` (13 files - good content)
   - Should be accessible but separate from architecture
   - **Solution:** Keep but reorganize, create proper README, link from main docs/README.md

7. **docs/ai/ mixing concerns** ⚠️
   - Agent operations, lessons, inconsistencies all mixed
   - This is AI agent memory, not user documentation
   - **Solution:** Keep but reorganize, move to supporting docs (not main index)

---

## Ideal Final Structure

```
docs/
├── README.md                    ← Main entry point (updated)
├── ROADMAP.md
├── 
├── design/                      ← Technical Architecture (20 canonical docs)
│   ├── README.md
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
│   │   ├── README.md
│   │   ├── ENDPOINT_CATALOG.md
│   │   ├── ERROR_CATALOG.md
│   │   └── RESPONSE_CODES.md       ← moved from docs/api/
│   └── patterns/
│       ├── PATTERNS.md
│       └── VALIDATION_STRATEGY.md
│
├── development/                 ← Developer Guides
│   ├── README.md
│   ├── FRONTEND_DEVELOPER_GUIDE.md
│   ├── ENVIRONMENT_SETUP.md
│   ├── TEST_STRATEGY.md
│   ├── DEBUG_GUIDE.md
│   └── CODE_STYLE.md
│
├── operations/                  ← Deployment & Operations
│   ├── README.md
│   ├── DEPLOYMENT_PIPELINE.md
│   ├── PRODUCTION_RUNBOOK.md
│   ├── DOCKER.md
│   └── SIGNING_AND_JWKS.md
│
├── security/                    ← Security & Compliance
│   ├── README.md
│   └── SECURITY_GUIDELINES.md
│
├── product/                     ← Product & Business Context (NEW organizational folder)
│   ├── README.md
│   ├── BOUNDED_CONTEXTS.md
│   ├── REQUERIMIENTOS.md
│   ├── PROPUESTA_SOLUCION.md
│   ├── GLOSARIO.md
│   └── flows/
│       ├── FLUJO_AUTENTICACION.md
│       ├── FLUJO_ACCOUNT.md
│       ├── FLUJO_TENANT_MANAGEMENT.md
│       └── FLUJO_BILLING.md
│
├── rfc/                         ← ALL RFCs in ONE place
│   ├── README.md                (Status matrix: which RFCs are active/approved/closed)
│   ├── IN_PROGRESS.md           (Current RFCs with status)
│   ├── restructure-multitenant/
│   │   ├── README.md
│   │   └── (12 files)
│   ├── account-ui-proposal/
│   │   ├── README.md
│   │   └── (5 files)
│   ├── t108-geoip-sessions/
│   │   ├── README.md
│   │   └── (4 files)
│   └── incomplete-sections/
│       ├── README.md
│       └── (2 files)
│
├── data/                        ← Database Documentation
│   ├── README.md
│   └── MIGRATIONS.md
│
├── archive/                     ← Historical & Deprecated
│   ├── README.md
│   ├── deprecated/              (14 outdated docs - from Phase 3)
│   │   ├── design/
│   │   ├── data/
│   │   ├── api/
│   │   └── development/
│   ├── rfc-history/             (Closed RFCs - for reference)
│   ├── email-templates/         (Email-specific docs)
│   └── research/                (Old research, investigations)
│
└── ai/                          ← AI Agent Operations (Supporting, not main navigation)
    ├── README.md
    ├── AGENT_OPERATIONS.md
    ├── lecciones/
    ├── propuestas/
    ├── agents-registro/
    └── inconsistencies/
```

---

## Migration Steps

### Step 1: Consolidate RFCs into docs/rfc/

**Current state:**
- `docs/rfc/` has 26 files (main RFCs)
- `docs/design/rfc-billing-contractor-refactor/` (embedded)
- `docs/design/rfc-restructure-implementation/` (embedded)

**Action:** Move embedded RFCs to docs/rfc/ with proper structure
```bash
mv docs/design/rfc-billing-contractor-refactor/ docs/rfc/billing-contractor-refactor/
mv docs/design/rfc-restructure-implementation/ docs/rfc/restructure-implementation/
```

### Step 2: Clean up duplicate README.md files

**Current:** 21 duplicate README.md files across folders  
**Action:** Keep ONLY 1 per folder, verify content, delete duplicates

### Step 3: Move orphaned docs to proper locations

**docs/api/ → docs/design/api/**
- AUTH_FLOW.md (consolidate or archive)
- BILLING_FLOW.md (consolidate or archive)
- OPENAPI.md (consolidate or archive)
- RESPONSE_CODES.md (already in ERROR_CATALOG.md, archive)

**Action:**
```bash
# Check if content is redundant with docs/design/api files first
# If yes: move to archive/
# If no: move to docs/design/api/
```

### Step 4: Archive email templates

**Current:** 11 files in docs/design/email/  
**Action:**
```bash
mkdir -p docs/archive/email-templates
mv docs/design/email/* docs/archive/email-templates/
rmdir docs/design/email
```

### Step 5: Reorganize product-design

**Current:** docs/product-design/ with 13 files (scattered)  
**Rename to:** docs/product/ and reorganize into subfolders

```bash
mkdir -p docs/product/flows
mv docs/product-design/* docs/product/
mv docs/product/FLUJO_*.md docs/product/flows/
rmdir docs/product-design
```

### Step 6: Clean up empty folders

**Remove from docs/ (move to root if needed):**
- docs/postman/ (move to /postman or .gitignore)
- docs/scripts/ (move to /scripts or .gitignore)
- docs/sql/ (move to /sql or .gitignore)
- docs/plans/ (empty, delete)
- docs/keygo-ui/ (almost empty, delete)

**Action:**
```bash
rm -rf docs/postman docs/scripts docs/sql docs/plans
# For keygo-ui: keep only ADR-001 if useful, else archive
```

### Step 7: Update all README.md files

**Create/update READMEs for:**
- docs/rfc/README.md (status matrix, link to IN_PROGRESS.md)
- docs/product/README.md (product context overview)
- docs/ai/README.md (clarify: AI operations, not user docs)
- docs/archive/README.md (update for new structure)

### Step 8: Update main docs/README.md

**Add sections:**
- Link to docs/product/ for product context
- Link to docs/rfc/IN_PROGRESS.md for active RFCs
- Clarify which docs are for users vs. internal ops (ai/)

---

## Files to Archive (Phase 4)

### Redundant with new structure:
- docs/api/AUTH_FLOW.md (if content is in OAUTH2_MULTIDOMAIN_CONTRACT.md)
- docs/api/BILLING_FLOW.md (if content is in product/)
- docs/api/OPENAPI.md (openapi spec is auto-generated, doc is redundant)
- docs/api/RESPONSE_CODES.md (consolidated in ERROR_CATALOG.md)

### Relocated:
- All `docs/design/email/*` → `docs/archive/email-templates/`
- `docs/design/T-111-implementation/` → `docs/plans/documentacion-2026/implementacion/T-111/` (active implementation plan)

---

## Summary

| Action | Count | Impact |
|---|---|---|
| **Consolidate RFCs** | 2 RFC dirs | Clear RFC organization |
| **Delete duplicate READMEs** | 21 files | Clean structure, no confusion |
| **Move to proper locations** | 4 files | Better organization |
| **Archive email templates** | 11 files | Less clutter, still available |
| **Rename product-design** | 13 files | Clear separation of concerns |
| **Remove empty folders** | 5 folders | Clean root |
| **New folder structure** | docs/product/ | Better discoverability |

---

## Result

**Before:** ~170 files, scattered structure, duplicates, confusion  
**After:** ~150 files, clear structure by audience, no redundancy, easy to navigate

---

## Ready to execute? Yes / No

This plan ensures:
✅ Single source of truth for each document  
✅ Clear organization by role/purpose  
✅ Archived docs still available for reference  
✅ No content loss, only reorganization  
✅ Better navigation for new developers
