# T-131 — Inventario de PII en el sistema

**Estado:** 🔲 PENDING  
**Módulos afectados:** `keygo-domain`, `keygo-supabase`, `keygo-api`, `keygo-app`, docs

---

## Problema / Requisito

No existe un catálogo formal de los datos personales identificables (PII) que maneja el sistema.
Identificarlos es necesario para:

- Verificar que ningún PII aparece en URLs (regla documentada en `security-guidelines.md`).
- Detectar campos PII que podrían estar siendo logueados accidentalmente.
- Evaluar cumplimiento GDPR/CCPA: minimización, retención, derecho al olvido.
- Guiar decisiones de enmascaramiento en responses (uso de `EmailMasker` y equivalentes).

## Solución Propuesta

1. Recorrer entidades de dominio, JPA entities y DTOs de request/response.
2. Clasificar cada campo PII encontrado por categoría y nivel de sensibilidad.
3. Registrar en qué superficies se expone (URL, body, response, logs, JWT claims).
4. Documentar en `doc/06-quality/pii-inventory.md`.

---

## Categorías de PII a identificar

| Categoría | Ejemplos en el sistema |
|---|---|
| Identificadores directos | `email`, `username` |
| Datos de identidad | `firstName`, `lastName` |
| Datos de contacto | `email`, `phone` (si aplica) |
| Credenciales | `password` (hash), `temporaryPassword`, tokens de recuperación |
| Identificadores técnicos | `userId`, `tenantId` (indirectamente identificadores) |
| Datos de sesión | IP, user-agent (en `sessions`) |
| Datos de billing | nombre del contractor, email del contractor |

---

## Pasos de Implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Recorrer entidades de dominio (`keygo-domain`) y listar campos PII | `keygo-domain/.../user/`, `billing/` | PENDING |
| 2 | Recorrer JPA entities (`keygo-supabase`) y verificar columnas con PII | `keygo-supabase/.../entity/` | PENDING |
| 3 | Recorrer request/response DTOs (`keygo-api`) y detectar PII expuesto en URLs | `keygo-api/.../request/`, `response/` | PENDING |
| 4 | Verificar JWT claims emitidos (`AuthorizationController`) | `keygo-api/.../auth/` | PENDING |
| 5 | Revisar logs (`log.info/error`) en use cases y adapters para detectar PII accidental | `keygo-app/`, `keygo-supabase/` | PENDING |
| 6 | Crear `doc/06-quality/pii-inventory.md` con tabla por entidad + nivel de sensibilidad | `doc/06-quality/` | PENDING |
| 7 | Actualizar `doc/06-quality/README.md` con la nueva entrada | `doc/06-quality/README.md` | PENDING |

---

## Verificación

- El inventario cubre todas las entidades de dominio y JPA entities.
- Ningún campo de categoría "Credenciales" aparece en responses (ni hasheado).
- Ningún PII aparece como path variable o query param en ningún controller.
- Los campos PII en responses de listados están enmascarados o justificados.
