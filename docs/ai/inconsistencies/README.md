# Inconsistencias Detectadas — Índice

**Propósito:** Registra lugares donde documentación o código no está sincronizado.

Base para priorizar correcciones y deuda técnica.

---

## 📍 Categorías de Inconsistencia

| Categoría | Descripción | Link |
|---|---|---|
| **Datos** | Schema, migraciones, modelo de datos | [`datos.md`](datos.md) |
| **APIs** | Endpoints, contratos, respuestas | [`apis.md`](apis.md) |
| **Documentación** | Docs que contradicen código | [`documentacion.md`](documentacion.md) |

---

## 🔴 Inconsistencias Críticas (A Corregir Primero)

| # | Tipo | Inconsistencia | Impacto | Estado |
|---|---|---|---|---|
| 1 | Datos | Schema en migrations vs entidades JPA | Alto | Ver `datos.md` |
| 2 | APIs | Endpoints en OpenAPI vs código | Medio | Ver `apis.md` |
| 3 | Docs | ARCHITECTURE.md vs código real | Medio | Ver `documentacion.md` |

---

## 📋 Cómo Usar

### Si encuentras inconsistencia

1. Categoriza: ¿datos, APIs, documentación?
2. Abre carpeta relevante
3. Agrega entrada con:
   - **Síntoma:** Qué no coincide
   - **Ubicación:** Dónde en código/docs
   - **Prioridad:** Alta/Media/Baja
   - **Solución:** Cómo corregir

### Si eres Tech Lead

1. Abre este README
2. Navega a categoría
3. Prioriza correcciones por impacto
4. Asigna como propuesta (T-NNN) si es crítica

---

## 🔗 Referencias Cruzadas

- **Propuestas:** [`../propuestas/README.md`](../propuestas/README.md) (registra T-NNN para inconsistencias críticas)
- **Lecciones:** [`../lecciones/README.md`](../lecciones/README.md) (aprende de errores)
- **Decisiones:** [`../../design/README.md`](../../design/README.md)

---

**Última actualización:** 2026-04-09  
**Total inconsistencias:** 10+  
**Responsable:** Equipo (reportar al detectar)
