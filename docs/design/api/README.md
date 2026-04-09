# API — Índice

**Propósito:** Contratos HTTP, errores, endpoints y política de versionado.

Referencia de cómo comunicar con la aplicación.

---

## 🎯 Búsqueda Rápida

| Pregunta | Documento |
|---|---|
| ¿Qué ResponseCode debería usar? | `ERROR_CATALOG.md` |
| ¿Cuáles son todos los endpoints? | `ENDPOINT_CATALOG.md` |
| ¿Cómo versiono la API? | `API_VERSIONING_STRATEGY.md` |
| ¿Cómo documento en OpenAPI? | Ver `/v3/api-docs` + `ERROR_CATALOG.md` |

---

## 📁 Documentos

| Documento | Estado | Contenido |
|---|---|---|
| `ERROR_CATALOG.md` | 🔲 Nuevo (Sprint 1) | ResponseCode, ErrorData, ejemplos OpenAPI |
| `ENDPOINT_CATALOG.md` | 🔲 Nuevo (Sprint 1) | Inventario endpoints por dominio |
| `API_VERSIONING_STRATEGY.md` | 🔲 Nuevo (Sprint 2) | Breaking changes, deprecation policy |

---

## 📊 Estructura de Error Response

```json
{
  "code": "INVALID_INPUT",
  "origin": "CLIENT_REQUEST",
  "clientMessage": "Localized message",
  "fieldErrors": [
    {
      "field": "email",
      "error": "invalid_format",
      "attemptedValue": "..."
    }
  ]
}
```

Ver `ERROR_CATALOG.md` para detalle.

---

## 🔗 Referencias Cruzadas

- **Decisiones:** [`../README.md`](../README.md)
- **Patrones de autorización:** [`../patterns/AUTHORIZATION_PATTERNS.md`](../patterns/AUTHORIZATION_PATTERNS.md)
- **Operaciones:** [`../../operations/README.md`](../../operations/README.md)
- **OpenAPI:** `/v3/api-docs` (runtime)

---

**Última actualización:** 2026-04-09  
**Estado:** Nuevos documentos (Sprint 1)
