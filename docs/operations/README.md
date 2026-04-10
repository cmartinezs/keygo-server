# Operations & Deployment Documentation

Guides for running KeyGo in production, deployment pipelines, and operational runbooks.

---

## 🎯 Búsqueda Rápida

| Necesidad | Documento |
|---|---|
| Ejecutar aplicación localmente | `DOCKER.md` |
| Deployar a producción | `PRODUCTION_RUNBOOK.md` (nuevo Sprint 2) |
| Rollback de versión | `PRODUCTION_RUNBOOK.md` (nuevo Sprint 2) |
| Configurar variables de ambiente | [`../development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md) |
| Observabilidad: logs, métricas | `OBSERVABILITY.md` (nuevo Sprint 3) |
| Firma de tokens JWT | `SIGNING_AND_JWKS.md` |
| Rotación de keys (T-028 KMS) | `SIGNING_AND_JWKS.md` (pendiente T-028) |

---

## 📁 Estructura

### 🐳 Docker y Runtime

| Documento | Descripción |
|---|---|
| `DOCKER.md` | Configuración Docker, networks, volumes, compose |

### 🚀 Deployment y Operación

| Documento | Descripción |
|---|---|
| `PRODUCTION_RUNBOOK.md` (nuevo Sprint 2) | Checklist deployment, rollback, troubleshooting prod |
| `SIGNING_AND_JWKS.md` | Cómo firmar JWT, publicar JWKS, key rotation |

### 📊 Observabilidad

| Documento | Descripción |
|---|---|
| `OBSERVABILITY.md` (nuevo Sprint 3) | Logs estructurados, métricas, tracing, dashboards |

---

## 📊 Estado de Documentos

### ✅ Existentes
- `DOCKER.md` — Setup local en containers

### 🔲 Nuevos (Sprint 2-3)
- `PRODUCTION_RUNBOOK.md` (Sprint 2) — Deployment production
- `OBSERVABILITY.md` (Sprint 3) — Logs, métricas, observabilidad

### 📋 Pendientes
- `SIGNING_AND_JWKS.md` — Existe pero no documentado en índice
- KMS integration (T-028) — Pendiente

---

## 🔄 Workflows Típicos

### Primer deployment

```
1. Leer: DOCKER.md (entender setup)
2. Leer: PRODUCTION_RUNBOOK.md (checklist)
3. Setup: Variables, secrets, BD
4. Deploy: Siguiendo checklist
5. Validar: Health checks, logs
```

### Monitoring en producción

```
1. Leer: OBSERVABILITY.md (cómo leer logs)
2. Dashboards: Prometheus/Grafana
3. Alertas: Configurar umbrales
4. Incident: PRODUCTION_RUNBOOK.md → Troubleshooting
```

### Rollback de emergency

```
1. Leer: PRODUCTION_RUNBOOK.md (sección Rollback)
2. Ejecutar: Pasos del runbook
3. Validar: Aplicación funciona
4. Investigar: ¿Qué causó el error?
5. Documentar: Lección en docs/ai/lecciones/
```

---

## 📚 Referencias Cruzadas

- **Setup local:** [`../development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md)
- **Debugging:** [`../development/DEBUG_GUIDE.md`](../development/DEBUG_GUIDE.md)
- **Propuestas:** [`../ai/propuestas/README.md`](../ai/propuestas/README.md) (T-020, T-028, T-073)
- **Lecciones:** [`../ai/lecciones/README.md`](../ai/lecciones/README.md)
- **Inconsistencias:** [`../ai/inconsistencies/README.md`](../ai/inconsistencies/README.md)

---

**Última actualización:** 2026-04-09  
**Estado:** Reorganización en curso + nuevos documentos (Sprint 2-3)  
**Responsable:** AI Agent
