# Documentación de Producto y Diseño — KeyGo Server

> Referencia única y centralizada de requisitos funcionales, diseño de sistemas y flujos operacionales para todo el proyecto KeyGo.

**Última actualización:** 2026-04-05  
**Responsable:** AI Agent + Equipo de Desarrollo

---

## 🗂️ Navegación Rápida

### 📋 **Situación y Análisis**
- **[SITUACION_ACTUAL.md](SITUACION_ACTUAL.md)** — Módulos, capacidades, entidades, endpoints, deuda técnica
- **[ANALISIS_DOLORES.md](ANALISIS_DOLORES.md)** — Problemas identificados, restricciones, causas raíz
- **[REQUERIMIENTOS.md](REQUERIMIENTOS.md)** — Funcionales y no-funcionales consolidados por dominio

### 🎯 **Diseño Estratégico**
- **[PROPUESTA_SOLUCION.md](PROPUESTA_SOLUCION.md)** — Roadmap consolidado con dependencias (fases 1-4)
- **[BOUNDED_CONTEXTS.md](BOUNDED_CONTEXTS.md)** — 4 dominios: Auth, Tenants, Billing, Account
- **[GLOSARIO.md](GLOSARIO.md)** — Términos, entidades, conceptos unificados
- **[DEPENDENCIAS.md](DEPENDENCIAS.md)** — Matriz de dependencias entre propuestas (T-NNN/F-NNN)

### 🎬 **Casos de Uso**
- **[CASOS_DE_USO.md](CASOS_DE_USO.md)** — Narrativas principales por contexto + mapa de actores

### 📊 **Diagramas Funcionales (Mermaid)**
- **[DIAGRAMAS/CASOS_DE_USO.md](DIAGRAMAS/CASOS_DE_USO.md)** — Diagrama UML de casos de uso
- **[DIAGRAMAS/FLUJO_AUTENTICACION.md](DIAGRAMAS/FLUJO_AUTENTICACION.md)** — Flujo OAuth2/OIDC completo
- **[DIAGRAMAS/FLUJO_TENANT_MANAGEMENT.md](DIAGRAMAS/FLUJO_TENANT_MANAGEMENT.md)** — Creación y gestión de tenants
- **[DIAGRAMAS/FLUJO_BILLING.md](DIAGRAMAS/FLUJO_BILLING.md)** — Suscripción y facturación
- **[DIAGRAMAS/FLUJO_ACCOUNT.md](DIAGRAMAS/FLUJO_ACCOUNT.md)** — Self-service (perfil, sesiones, password)

### 📑 **Diagramas de Secuencia**
- **[DIAGRAMAS/SECUENCIAS/LOGIN_CODE_GRANT.md](DIAGRAMAS/SECUENCIAS/LOGIN_CODE_GRANT.md)** — Authorization code flow
- **[DIAGRAMAS/SECUENCIAS/CLIENT_CREDENTIALS.md](DIAGRAMAS/SECUENCIAS/CLIENT_CREDENTIALS.md)** — M2M flow
- **[DIAGRAMAS/SECUENCIAS/TENANT_CREATION.md](DIAGRAMAS/SECUENCIAS/TENANT_CREATION.md)** — Onboarding de tenant
- **[DIAGRAMAS/SECUENCIAS/CONTRACT_ACTIVATION.md](DIAGRAMAS/SECUENCIAS/CONTRACT_ACTIVATION.md)** — Activar suscripción
- **[DIAGRAMAS/SECUENCIAS/PASSWORD_RESET.md](DIAGRAMAS/SECUENCIAS/PASSWORD_RESET.md)** — Flujo forgot/recover/reset

### 🔄 **Máquinas de Estado (State Machines)**
- **[DIAGRAMAS/ESTADOS/USUARIO.md](DIAGRAMAS/ESTADOS/USUARIO.md)** — Estados de usuario (ACTIVE, RESET_PASSWORD, SUSPENDED, DELETED)
- **[DIAGRAMAS/ESTADOS/SESION.md](DIAGRAMAS/ESTADOS/SESION.md)** — Ciclo de vida de sesión
- **[DIAGRAMAS/ESTADOS/SUSCRIPCION.md](DIAGRAMAS/ESTADOS/SUSCRIPCION.md)** — Estados de contrato de billing
- **[DIAGRAMAS/ESTADOS/TENANT.md](DIAGRAMAS/ESTADOS/TENANT.md)** — Ciclo de tenant (ONBOARDING → ACTIVE → SUSPENDED)

### 🔌 **Integraciones Futuras**
- **[INTEGRACIONES/README.md](INTEGRACIONES/README.md)** — Índice de integraciones planificadas
- **[INTEGRACIONES/KMS.md](INTEGRACIONES/KMS.md)** — Gestión de claves criptográficas (T-028)
- **[INTEGRACIONES/PAYMENT_GATEWAY.md](INTEGRACIONES/PAYMENT_GATEWAY.md)** — Integración Stripe/MercadoPago (T-084)
- **[INTEGRACIONES/SCIM.md](INTEGRACIONES/SCIM.md)** — SCIM 2.0 para aprovisionamiento (T-047)
- **[INTEGRACIONES/OBSERVABILIDAD.md](INTEGRACIONES/OBSERVABILIDAD.md)** — OpenTelemetry + Prometheus (T-020)

---

## 📊 Estado General del Proyecto (Snapshot 2026-04-05)

| Aspecto | Estado | Detalle |
|---|---|---|
| **Propuestas Completadas** | 41 ✅ | Corto plazo: 20, Mediano: 18, Largo: 3 |
| **Propuestas Pendientes** | 107 🔲 | Corto plazo: 37, Mediano: 28, Largo: 42 |
| **Capacidades Core** | 85% ✅ | Auth, Tenants, Account funcionales. Billing base ✅ |
| **Deuda Técnica** | Baja 📉 | 3 inconsistencias no-críticas, sin lint enforcement |
| **Cobertura Tests** | 45% | JaCoCo. Roadmap: aumentar a 70% (T-115) |
| **Documentación** | Mejorada 📈 | Este documento centraliza todo |

---

## 🚀 Cómo Usar Esta Documentación

### **Para Implementar una Propuesta (T-NNN/F-NNN)**
1. Leer **[SITUACION_ACTUAL.md](SITUACION_ACTUAL.md)** — contexto actual
2. Leer **[REQUERIMIENTOS.md](REQUERIMIENTOS.md)** — qué necesita el dominio
3. Revisar **[DEPENDENCIAS.md](DEPENDENCIAS.md)** — qué propuestas bloquean ésta
4. Consultar diagramas relevantes en **[DIAGRAMAS/](DIAGRAMAS/)** — flujo visual
5. Implementar según plan en **[PROPUESTA_SOLUCION.md](PROPUESTA_SOLUCION.md)**

### **Para Entender un Dominio**
1. **Auth:** Leer [FLUJO_AUTENTICACION.md](DIAGRAMAS/FLUJO_AUTENTICACION.md) + [LOGIN_CODE_GRANT.md](DIAGRAMAS/SECUENCIAS/LOGIN_CODE_GRANT.md)
2. **Tenants:** Leer [FLUJO_TENANT_MANAGEMENT.md](DIAGRAMAS/FLUJO_TENANT_MANAGEMENT.md) + [TENANT_CREATION.md](DIAGRAMAS/SECUENCIAS/TENANT_CREATION.md) + [TENANT.md](DIAGRAMAS/ESTADOS/TENANT.md)
3. **Billing:** Leer [FLUJO_BILLING.md](DIAGRAMAS/FLUJO_BILLING.md) + [CONTRACT_ACTIVATION.md](DIAGRAMAS/SECUENCIAS/CONTRACT_ACTIVATION.md) + [SUSCRIPCION.md](DIAGRAMAS/ESTADOS/SUSCRIPCION.md)
4. **Account:** Leer [FLUJO_ACCOUNT.md](DIAGRAMAS/FLUJO_ACCOUNT.md) + [PASSWORD_RESET.md](DIAGRAMAS/SECUENCIAS/PASSWORD_RESET.md)

### **Para Onboarding de Nuevos Devs**
1. Leer **[GLOSARIO.md](GLOSARIO.md)** — términos clave
2. Leer **[BOUNDED_CONTEXTS.md](BOUNDED_CONTEXTS.md)** — dominios y responsabilidades
3. Leer **[SITUACION_ACTUAL.md](SITUACION_ACTUAL.md)** — qué existe hoy
4. Explorar diagramas para entender flujos

---

## 📎 Relación con Documentación Existente

| Documento Existente | Relación con product-design | Acción |
|---|---|---|
| `ROADMAP.md` | ← Propuestas detalladas en REQUERIMIENTOS + PROPUESTA_SOLUCION | Mantener como source of truth para IDs T-NNN/F-NNN |
| `AI_CONTEXT.md` | ← Resumen ejecutivo en SITUACION_ACTUAL | Actualizar referencias a product-design |
| `ARCHITECTURE.md` | ← Bounded contexts + diseño en este directorio | Mantener pero referenciar product-design para detalle |
| `AGENTS.md` | ← Detalles técnicos en SITUACION_ACTUAL | Mantener como quick-start; referenciar product-design |
| `docs/api/AUTH_FLOW.md` | = Diagrama en FLUJO_AUTENTICACION.md | Consolidar en product-design |
| `docs/design/EXCEPTION_HIERARCHY.md` | = Referencia en REQUERIMIENTOS (no-funcionales) | Mantener separado |
| `docs/data/DATA_MODEL.md` | = Entidades en SITUACION_ACTUAL | Mantener; agregar referencias desde product-design |

---

## 🔄 Ciclo de Actualización

**Esta documentación se actualiza cuando:**
1. Se completa una propuesta importante (T-NNN/F-NNN ✅)
2. Se detecta inconsistencia entre docs y código
3. Cambian requerimientos o se validan nuevos dolores
4. Se consolida feedback de implementación

**Responsable:** AI Agent (automático) + Equipo de Desarrollo (manual)  
**Frecuencia recomendada:** Revisión cada 2 semanas o post-milestone

---

**¿Dónde debo empezar?** → Ve a **[SITUACION_ACTUAL.md](SITUACION_ACTUAL.md)**
