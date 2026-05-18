# SPEC Backend P0 — Validación contractual de configuración OAuth en apps

| Campo | Valor |
|---|---|
| ID | `BE-P0-004` |
| Tipo | Backend specification |
| Prioridad | P0 / Bloqueante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / apps module |
| Módulo sugerido | ClientApp create/update validation |
| Estado | Propuesta para implementación |

## Problema

Las apps deben configurar redirect URIs, grants y scopes. Backend recibe `redirectUris`, `grants` y `scopes` al crear/actualizar app, pero se requiere asegurar validaciones mínimas para que una app con `AUTHORIZATION_CODE` no quede no integrable.

## Decisión funcional

Cuando una app habilite `AUTHORIZATION_CODE`, debe tener al menos una redirect URI válida. Además, scopes OIDC base deben estar disponibles o ser sugeridos por defecto desde frontend.

## Alcance incluido

- Validar create/update de app.
- Rechazar configuración inválida con error `400 Bad Request` y mensaje claro.
- Normalizar grants/scopes si aplica.
- Garantizar que `PUBLIC` use PKCE para Authorization Code.

## Fuera de alcance

- Crear UI.
- Implementar consentimiento OIDC avanzado.
- Implementar dynamic client registration.

## Reglas de validación sugeridas

| Caso | Resultado esperado |
|---|---|
| `AUTHORIZATION_CODE` sin redirect URI | `400 Bad Request` |
| redirect URI mal formada | `400 Bad Request` |
| app `PUBLIC` con Authorization Code sin PKCE requerido | Rechazar o forzar PKCE |
| scopes vacíos | Permitir solo si hay default backend documentado; recomendado: default OIDC mínimo |
| `CLIENT_CREDENTIALS` en app `PUBLIC` | Rechazar salvo decisión explícita |

## Error response sugerido

```json
{
  "code": "KG-APP-OAUTH-CONFIG-INVALID",
  "message": "Authorization Code requires at least one valid redirect URI.",
  "details": [
    {
      "field": "redirect_uris",
      "reason": "required_when_authorization_code_is_enabled"
    }
  ]
}
```

## Instrucciones para AI Agent

1. Revisar DTOs de create/update client app.
2. Revisar validaciones actuales.
3. Agregar validación condicional por grant.
4. Revisar serialización snake_case/camelCase si aplica.
5. Agregar tests de create/update.
6. Verificar que el frontend pueda mostrar errores por campo.

## Criterios de aceptación

- No se puede crear app Authorization Code sin redirect URI.
- No se puede dejar una app pública con configuración incompatible.
- Errores son claros para que la UI los muestre.
- No se rompe creación de app confidential con client credentials.

## Pruebas sugeridas

- Create public app + auth code + redirect URI válida: OK.
- Create public app + auth code + redirect URI vacía: 400.
- Create confidential app + client credentials: OK.
- Update app quitando redirect URI con auth code activo: 400.

## Definition of Done

- Contrato backend robusto.
- Tests cubren validaciones críticas.
- Documentación API actualizada si existe.
