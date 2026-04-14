# BE-007 — Platform Account Profile Endpoints

**Fecha:** 2026-04-14  
**Estado:** 🟢 Confirmado  
**Tarea:** [T-153](../../09-ai/tasks/completed/T-153-platform-account-profile.md)

## Cambio

Backend ha implementado dos nuevos endpoints de perfil self-service a nivel de plataforma:

```
GET  /api/v1/platform/account/profile
     Authorization: Bearer <access_token>
     Retorna el perfil completo del platform user autenticado
     
PATCH /api/v1/platform/account/profile
      Authorization: Bearer <access_token>
      Actualiza parcialmente el perfil (PATCH semántica: solo campos no-nulos)
```

### Response DTO: `UserProfileData`

```json
{
  "id": "uuid",
  "tenant_id": null,
  "username": "user@domain.com",
  "email": "user@domain.com",
  "first_name": "John",
  "last_name": "Doe",
  "status": "ACTIVE",
  "phone_number": "+1234567890",
  "locale": "es-MX",
  "zoneinfo": "America/Mexico_City",
  "profile_picture_url": "https://...",
  "birthdate": null,
  "website": null
}
```

**Nota:** Para platform users, los campos `tenant_id`, `birthdate` y `website` son siempre `null`.

### Request DTO: `UpdateUserProfileRequest` (PATCH)

```json
{
  "first_name": "Jane",
  "last_name": "Smith",
  "phone_number": "+9876543210",
  "locale": "en-US",
  "zoneinfo": "America/New_York",
  "profile_picture_url": "https://..."
}
```

Todos los campos son opcionales. Solo se actualizan los campos presentes en la request.

### Códigos de Respuesta

| Código | Mensajes |
|---|---|
| `200 OK` | `USER_PROFILE_RETRIEVED` (GET), `USER_PROFILE_UPDATED` (PATCH) |
| `401 Unauthorized` | `AUTHENTICATION_REQUIRED` — Token ausente, inválido o expirado |
| `404 Not Found` | `RESOURCE_NOT_FOUND` — Platform user no encontrado (raro; indica inconsistencia en token) |

## Impacto en UI

1. **Ubicación en UI:** Panel de perfil personal del usuario de plataforma (keygo-UI)
2. **Funcionalidad disponible:**
   - Ver perfil propio del usuario de plataforma
   - Editar nombre, teléfono, localización y foto de perfil sin afectar la account de tenant
3. **Cambios de contrato:**
   - `tenant_id` será siempre `null` en profile (diferente de `/tenants/{slug}/account/profile`)
   - Los campos editables son: `first_name`, `last_name`, `phone_number`, `locale`, `zoneinfo`, `profile_picture_url`
   - No es posible editar: `email`, `username`, `birthdate`, `website` (no existen en `PlatformUser`)
4. **Headers requeridos:**
   - `Authorization: Bearer <access_token>` — Token JWT de plataforma (obtenido en login de plataforma)
5. **CORS:** Ya está cubierto por política general de CORS del backend

## Integración

Este endpoint está **listo para consumir inmediatamente**. No requiere cambios adicionales en el backend. La semántica es idéntica a los endpoints de tenant profile, pero operan sobre usuarios de plataforma.

## Verificación

Backend ha verificado:
- ✅ Compilación exitosa sin errores
- ✅ Endpoints documentados en OpenAPI/Swagger
- ✅ Inyección de dependencias correctamente configurada
- ✅ Reutilización de DTOs y códigos de respuesta existentes
- ✅ Seguridad: Bearer token verificado y claim `sub` extraído

## Confirmación

✅ **Confirmado por Backend:** Implementación completada y verificada en T-153 (2026-04-14).

**Próximo paso:** UI consume los endpoints para la funcionalidad de edición de perfil personal.
