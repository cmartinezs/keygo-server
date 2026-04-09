# Patrones — Índice

**Propósito:** Cómo hacemos las cosas: patrones, convenciones y anti-patterns.

Guía para implementar features consistentemente con el resto del proyecto.

---

## 🎯 Búsqueda Rápida

| Pregunta | Documento |
|---|---|
| ¿Cuáles son los patrones del proyecto? | `PATTERNS.md` |
| ¿Dónde va validación de dominio? | `VALIDATION_STRATEGY.md` |
| ¿Cómo uso @PreAuthorize? | `AUTHORIZATION_PATTERNS.md` |
| ¿Cómo mapeo excepciones a errores? | `ERROR_HANDLING.md` |
| ¿Cuáles son los anti-patterns a evitar? | `PATTERNS.md` (sección anti-patterns) |

---

## 📁 Documentos

| Documento | Contenido |
|---|---|
| `PATTERNS.md` (nuevo) | Patrones adoptados + anti-patterns consolidados |
| `VALIDATION_STRATEGY.md` (nuevo) | Bean Validation vs lógica de dominio vs use case |
| `AUTHORIZATION_PATTERNS.md` (nuevo) | @PreAuthorize, tenant match, RBAC matrix |
| `ERROR_HANDLING.md` (nuevo) | Mapeo de excepciones a ResponseCode |

---

## ✅ Checklist Antes de Implementar Feature

1. ¿Dónde va la validación? → `VALIDATION_STRATEGY.md`
2. ¿Cómo implemento autorización? → `AUTHORIZATION_PATTERNS.md`
3. ¿Qué ResponseCode retorno? → `ERROR_HANDLING.md` + `../api/ERROR_CATALOG.md`
4. ¿Hay patrón similar? → `PATTERNS.md`
5. ¿Qué anti-patterns evitar? → `PATTERNS.md` (sección anti-patterns)

---

## 🔗 Referencias Cruzadas

- **Decisiones:** [`../README.md`](../README.md)
- **API:** [`../api/ERROR_CATALOG.md`](../api/ERROR_CATALOG.md)
- **Lecciones:** [`../../ai/lecciones/README.md`](../../ai/lecciones/README.md)

---

**Última actualización:** 2026-04-09  
**Estado:** Nuevos documentos (Sprint 1)
