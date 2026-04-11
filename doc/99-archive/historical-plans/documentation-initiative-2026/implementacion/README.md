# Planes de Implementación — Índice

Documentación de propuestas específicas de implementación, desglose de tareas y checklists de completación.

---

## 📋 Planes Disponibles

### T-111 — Modelo RBAC Multi-Ámbito
**Status:** 📋 In Progress  
**Objetivo:** Implementar 4 tablas nuevas (platform_roles, platform_user_roles, tenant_roles, tenant_user_roles)  
**Esfuerzo:** ~36 horas (4-5 días)  
**Ubicación:** [`T-111/`](T-111/)

**Archivos:**
- [`T-111/PLAN.md`](T-111/PLAN.md) — Plan detallado con desglose A-J
- [`T-111/MODEL.md`](T-111/MODEL.md) — Diagrama ER y especificación técnica
- [`T-111/IMPLEMENTATION_CHECKLIST.md`](T-111/IMPLEMENTATION_CHECKLIST.md) — Checklist de progreso
- [`T-111/INTEGRATION_PLAN.md`](T-111/INTEGRATION_PLAN.md) — Integración con sistema
- [`T-111/RELATIONS.md`](T-111/RELATIONS.md) — Mapeo completo de relaciones

---

## 🎯 Cómo Usar Este Directorio

### Para Entender una Propuesta
1. Navega a la carpeta `T-XXX/`
2. Lee `README.md` (si existe) o el resumen en este índice
3. Lee `PLAN.md` para entender el alcance

### Para Implementar
1. Lee `PLAN.md` completo
2. Abre `IMPLEMENTATION_CHECKLIST.md` y márcalo mientras trabajas
3. Consulta `MODEL.md` para detalles técnicos
4. Actualiza el checklist a medida que avanzas

### Para Validar
1. Ejecuta las validaciones en `IMPLEMENTATION_CHECKLIST.md` — sección "Validaciones Finales"
2. Compara con `MODEL.md` para asegurar que la implementación coincida
3. Marcar como ✅ COMPLETADO cuando todo pase

---

## 📊 Estado General

| Propuesta | Estado | Esfuerzo | Comienzo |
|---|---|---|---|
| T-111 | 📋 In Progress | 36h | 2026-04-06 |

---

## 🔗 Relacionados

- **RFC de origen:** [`../../rfc/restructure-multitenant/`](../../rfc/restructure-multitenant/)
- **Arquitectura:** [`../../design/ARCHITECTURE.md`](../../design/ARCHITECTURE.md)
- **Roadmap:** [`../../../../ROADMAP.md`](../../../../ROADMAP.md)

---

**Última actualización:** 2026-04-09  
**Responsable:** AI Agent + Development Team
