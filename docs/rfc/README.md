# RFCs — Request for Comments & Design Decisions

Central repository for all architectural decisions, design proposals, and implementation plans.

---

## 📋 Quick Navigation

**Looking for...**
- **In-progress RFCs?** → [`IN_PROGRESS.md`](IN_PROGRESS.md)
- **Specific RFC by topic?** → See categories below
- **Closed/historical RFCs?** → [`../../archive/rfc-history/`](../../archive/rfc-history/)

---

## 🗂️ RFC Categories

### 🏗️ Restructuring & Architecture (12 files)

| RFC | Status | Purpose |
|---|---|---|
| [`restructure-multitenant/`](restructure-multitenant/) | ✅ Active | Multi-tenant refactoring, service isolation, domain model |
| [`restructure-implementation/`](restructure-implementation/) | 📋 In Progress | Implementation phases, migration strategy |
| [`billing-contractor-refactor/`](billing-contractor-refactor/) | 📋 In Progress | Billing system refactoring, contractor workflow |

### 💡 Features & Proposals (11 files)

| RFC | Status | Purpose |
|---|---|---|
| [`account-ui-proposal/`](account-ui-proposal/) | 🔍 Review | UI proposal for account management |
| [`t108-geoip-sessions/`](t108-geoip-sessions/) | ✅ Active | Geo-IP session tracking & analytics |

### 📝 Incomplete/Pending (2 files)

| RFC | Status | Purpose |
|---|---|---|
| [`incomplete-sections/`](incomplete-sections/) | ⏸️ Pending | Sections awaiting completion/review |

---

## 📊 Status Matrix

| Status | Count | Meaning |
|---|---|---|
| ✅ Active | 3 | Currently being implemented or maintained |
| 📋 In Progress | 2 | Under active development, awaiting completion |
| 🔍 Review | 1 | Awaiting architectural review & decision |
| ⏸️ Pending | 2 | Awaiting inputs, blocked, or incomplete |
| 🏁 Closed | TBD | See `archive/rfc-history/` |

---

## 🚀 How to Use

### For Architects / Tech Leads
1. Review [`IN_PROGRESS.md`](IN_PROGRESS.md) for current decisions
2. Link to specific RFC folders in technical discussions
3. Update status in [`IN_PROGRESS.md`](IN_PROGRESS.md) as decisions are made

### For Developers
1. Find RFC relevant to your task in categories above
2. Read the README.md inside each RFC folder
3. Check [`IN_PROGRESS.md`](IN_PROGRESS.md) for implementation timeline

### For Documentation
1. RFCs become canonical references in [`../design/`](../design/) once approved
2. Update cross-references when RFCs are closed
3. Archive closed RFCs to `archive/rfc-history/` with decision date

---

## 📌 RFC Structure

Each RFC folder contains:
- `README.md` — Overview, decision summary, timeline
- `ANALYSIS.md` or `PROPOSAL.md` — Detailed analysis
- Supporting documents (diagrams, examples, alternatives)
- `DECISION.md` — Final decision & rationale (when closed)

---

## 🔗 Related

- **Active Decisions** → [`IN_PROGRESS.md`](IN_PROGRESS.md)
- **Archived RFCs** → [`../../archive/rfc-history/`](../../archive/rfc-history/)
- **Architecture** → [`../design/ARCHITECTURE.md`](../design/ARCHITECTURE.md)
- **Decision Log** → Linked from [`../design/RFC_CLOSURE_PROCESS.md`](../design/RFC_CLOSURE_PROCESS.md)

---

**Last updated:** 2026-04-09  
**Total RFCs:** 6 active + 2 pending  
**Maintained by:** Architecture Team
