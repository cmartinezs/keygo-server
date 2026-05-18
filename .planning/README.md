# 📋 Planning

Central directory for all project plannings. Every planning follows a three-phase lifecycle and maintains its own term traceability matrix.

> For detailed structure, lifecycle, and naming conventions, see [`GUIDE.md`](GUIDE.md).

---

## 🚨 Fundamental Rule

> **Nothing is executed without being inside a planning.**

Before performing any action in this repository — generating a phase document, modifying a template, refactoring a guide, updating a process — there must be a task in a scope of an active planning that covers it.

### Bypass Parameters

When a prompt contains one of these parameters (at the start or end):

| Parameter | Behavior |
|-----------|----------|
| `--no-plan` | Ask: *"Are you sure you want to proceed without a planning entry?"*. If confirmed → execute. If not → do nothing. |
| `--no-plan-force` | Execute directly without asking or planning. |

**If something is requested that is not in any planning (and without a bypass parameter):**
1. Stop execution.
2. Ask: is this part of an existing planning or a new one?
3. If part of an existing one → identify which scope and task it belongs to, and wait for the flow to reach it.
4. If it's new → create the planning (at minimum the Initial phase) before executing.

---

## 📂 Plannings

> **In progress** (EXPANSION / DEEPENING): [`active/`](active/README.md) · **Completed**: [`finished/`](finished/README.md)

### 🌱 INITIAL (pendientes de dimensionar)

| ID | Prefijo | Nombre | Prioridad |
|----|---------|--------|-----------|
| [008](008-back-username-review/00-initial.md) | BACK | Username Review — auditoría y normalización del campo username | P2 / Deuda técnica |

### 🚧 In Progress → see [`active/README.md`](active/README.md)

| ID | Prefijo | Nombre | Prioridad |
|----|---------|--------|-----------|
| [002](active/002-ui-p0-marcha-blanca/README.md) | UI | Marcha Blanca — Frontend P0 | P0 / Bloqueante |
| [003](active/003-back-p1-marcha-blanca/README.md) | BACK | Marcha Blanca — Backend P1 | P1 / Importante |
| [004](active/004-ui-p1-marcha-blanca/README.md) | UI | Marcha Blanca — Frontend P1 | P1 / Importante |
| [005](active/005-qa-marcha-blanca/README.md) | QA | Marcha Blanca — Quality Gates | P0/P1 |
| [006](active/006-back-fix-resend-verification-contract/README.md) | BACK | Fix contrato resend-verification | P1 / Bug bloqueante |
| [007](active/007-back-refactor-verification-email/README.md) | BACK | Refactor envío de email de verificación | P2 / Deuda técnica |

### ✅ Completed → see [`finished/README.md`](finished/README.md)

| ID | Prefijo | Nombre | Prioridad |
|----|---------|--------|-----------|
| [001](finished/001-back-p0-marcha-blanca/README.md) | BACK | Marcha Blanca — Backend P0 | P0 / Bloqueante |

---

## 🔄 Workflows

Every task within a planning follows a defined workflow. The complete catalog of workflows and sub-workflows is in [`WORKFLOWS/README.md`](WORKFLOWS/README.md).

Workflow types are referenced in the **Workflow** field of each task within a scope.

For the vocabulary of the planning system, see the **[`GLOSSARY.md`](GLOSSARY.md)** — operational glossary (planning, scope, workflow, PDR, done, etc.).

---
