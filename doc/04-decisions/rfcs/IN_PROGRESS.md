# RFC Status — In Progress

Current architectural decisions, proposals, and their implementation timeline.

---

## 📋 Active RFCs

### 1️⃣ ddl-full-refactor/
**Status:** 📋 In Progress  
**Purpose:** Realinear todo el backend al DDL vigente  
**Timeline:** 2026-04 a 2026-Q2  
**Next Step:** Cerrar Fase 0 y Fase 1 de persistencia  
**See:** [`./ddl-full-refactor/README.md`](./ddl-full-refactor/README.md)

---

### 2️⃣ restructure-multitenant/
**Status:** ✅ Active  
**Purpose:** Multi-tenant service restructuring  
**Timeline:** Sprint 1-4 (2026-Q2)  
**Decision:** Approved — Multi-domain contract pattern  
**See:** [`./restructure-multitenant/README.md`](./restructure-multitenant/README.md)

---

### 3️⃣ restructure-implementation/
**Status:** 📋 In Progress  
**Purpose:** Phase-by-phase implementation strategy  
**Timeline:** Ongoing  
**Next Step:** Complete Phase 2 by 2026-04-15  
**See:** [`./restructure-implementation/README.md`](./restructure-implementation/README.md)

---

### 4️⃣ billing-contractor-refactor/
**Status:** 📋 In Progress  
**Purpose:** Refactor billing for contractor workflows  
**Timeline:** Sprint 2-3  
**Next Step:** Design review with stakeholders  
**See:** [`./billing-contractor-refactor/README.md`](./billing-contractor-refactor/README.md)

---

### 5️⃣ t108-geoip-sessions/
**Status:** ✅ Active  
**Purpose:** Geo-IP tracking & session analytics  
**Timeline:** T-108 (backlog priority)  
**Decision:** Pending — Awaiting implementation kickoff  
**See:** [`./t108-geoip-sessions/README.md`](./t108-geoip-sessions/README.md)

---

### 6️⃣ account-ui-proposal/
**Status:** 🔍 Review  
**Purpose:** Account management UI proposal  
**Timeline:** Sprint 4+  
**Decision:** Pending — Awaiting review  
**See:** [`./account-ui-proposal/README.md`](./account-ui-proposal/README.md)

---

### 7️⃣ rbac-multi-scope-alignment/
**Status:** 📋 Draft  
**Purpose:** Alinear el RBAC multi-ambito con el baseline, flows y contratos vigentes  
**Decision:** Pending — Revisar catalogo canonico, tenant RBAC API y read models de autorizacion  
**See:** [`./rbac-multi-scope-alignment/README.md`](./rbac-multi-scope-alignment/README.md)

---

## ⏸️ Pending / Incomplete

### incomplete-sections/
**Status:** ⏸️ Pending  
**Purpose:** Sections awaiting completion  
**Blocker:** Technical clarifications needed  
**See:** [`./incomplete-sections/README.md`](./incomplete-sections/README.md)

---

## 📅 Implementation Timeline

| Month | RFC | Phase | Owner |
|---|---|---|---|
| 2026-04 | ddl-full-refactor | Fase 0–1 | @Backend |
| 2026-04 | restructure-multitenant | Implementation Phase 2 | @Architecture |
| 2026-04 | restructure-implementation | Design Phase 2 | @Architecture |
| 2026-05 | billing-contractor-refactor | Implementation Phase 1 | @Backend |
| 2026-Q3 | t108-geoip-sessions | Backlog | @TBD |
| 2026-Q3+ | account-ui-proposal | Under Review | @Frontend |

---

## 🔄 Decision Flow

```
RFC Created
    ↓
Design Review
    ↓
Decision: Approve / Reject / Request Changes
    ↓
If Approved:
  - Move to RFC_CLOSURE_PROCESS
  - Create canonical doc in ../design/
  - Archive RFC to archive/rfc-history/
    ↓
If Rejected:
  - Document decision in RFC
  - Archive to archive/rfc-history/CLOSED/
```

---

## 📞 Next Steps

**For Architects:**
- Review pending RFCs in the "Review" status
- Schedule decisions on account-ui-proposal and incomplete-sections
- Update timeline if milestones shift

**For Developers:**
- Check timeline to know when features become actionable
- Reference active RFCs in implementation PRs
- Update this document when RFC status changes

---

**Last updated:** 2026-04-09  
**Next review:** 2026-04-15
