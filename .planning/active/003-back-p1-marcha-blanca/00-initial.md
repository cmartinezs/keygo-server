# 🌱 INITIAL: BACK-003 — Marcha Blanca Backend P1

> **Status:** DEEPENING
> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Intent

Implementar las 6 mejoras backend P1 necesarias para que el piloto KeyGo opere sin fricción: contrato consistente de acciones de estado de usuario, endpoint de sesiones admin, búsqueda paginada de usuarios y apps, política de aprobación de memberships, política de acceso por app, y contrato de revocación OAuth/logout.

---

## Why

Sin estas mejoras, el piloto puede operar pero con limitaciones operativas significativas: selects limitados a 20 registros, logout solo cosmético, flujo de membership sin aprobación operable, y acceso sin control de política por app. Son condiciones P1 según el diagnóstico.

---

## Approximate Scope

- User module: acciones suspend/activate
- Session module: endpoint admin de sesiones
- Users/Apps list endpoints: búsqueda por `q`
- Membership module: aprobación, rechazo
- ClientApp module: campo `access_policy`
- OAuth/Auth module: revocación de tokens, logout

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-17
- **Related planning:** 001-back-p0-marcha-blanca (debe estar completado)

---

## Next Step

- [x] Dimensionado → ver `01-expansion.md` y `02-deepening/`

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
