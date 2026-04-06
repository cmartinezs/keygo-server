# 06. JWT y estrategia de autorización

## Objetivo

Definir qué conviene incluir en el JWT de Keygo y cómo equilibrar tamaño de token, ergonomía del frontend y control real de autorización.

---

## 1. Problema a resolver

Cuando se introducen roles, permisos y jerarquías, aparece una duda natural:

- ¿se deben enviar todos los permisos en el JWT?
- ¿basta con enviar roles?
- ¿el frontend necesita capacidades adicionales?
- ¿cómo evitar que el token crezca demasiado?

La propuesta de este documento es resolverlo con una estrategia pragmática.

---

## 2. Principio general recomendado

Se recomienda que el token sea **informativo y compacto**, mientras que la autorización real se resuelva principalmente en backend.

En otras palabras:

- el token debe decir **quién es** el usuario,
- en qué **contexto** opera,
- y qué **rol** tiene,
- pero no debe convertirse en una copia serializada de toda la matriz de permisos.

---

## 3. Qué se recomienda incluir

Dependiendo del tipo de token, la semántica cambia.

## 3.1 ID Token
Debe servir principalmente para identidad.

### Claims típicos
- `sub`
- `name`
- `email`
- `email_verified`
- `sid` si aplica
- `auth_time` si aplica

## 3.2 Access Token
Debe servir para autorización hacia una audiencia concreta.

### Claims recomendados para Keygo
- `iss`
- `sub`
- `aud`
- `sid`
- `tenant_id` o contexto equivalente si corresponde
- `roles`

### Opcional
- `capabilities` resumidas para la UI, si realmente ayudan

## 3.3 Refresh Token
No debería usarse como token descriptivo para UI.  
Se recomienda tratarlo como un artefacto opaco o, al menos, como un token con semántica restringida al flujo de renovación.

---

## 4. Qué no se recomienda incluir

No se recomienda incluir listas extensas de permisos finos del tipo:

- `tenant.user.create`
- `tenant.user.update`
- `tenant.user.delete`
- `tenant.billing.invoice.read`
- `tenant.billing.payment_method.update`

### Motivos
- el token crece innecesariamente,
- aumenta el acoplamiento entre autorización y transporte,
- dificulta ajustes futuros,
- y deja más información sensible o técnica circulando por requests.

---

## 5. Estrategia recomendada para Keygo

La estrategia sugerida es:

### Opción principal
Enviar en el `access_token`:

- identidad básica,
- contexto,
- y roles.

### Opción complementaria
Si la UI necesita simplificar renderizado, enviar además un conjunto acotado de capacidades resumidas.

Ejemplos de capacidades resumidas:
- `profile:self`
- `users:manage`
- `apps:manage`
- `billing:read`
- `billing:manage`

Estas capacidades no deben ser una lista exhaustiva de permisos atómicos, sino una vista operativa para el frontend.

---

## 6. Autorización en backend

La autorización real debe resolverse en backend a partir de:

- roles del usuario,
- herencia entre roles,
- y matriz de permisos o capacidades efectivas.

Esto permite:

- centralizar reglas,
- modificar permisos sin rehacer contratos de token,
- y mantener más bajo el costo de evolución.

---

## 7. Renderizado del frontend

El frontend puede usar:

- roles,
- o capacidades resumidas,
- o un endpoint complementario `/me` o `/me/authorization`

para decidir qué secciones mostrar.

### Recomendación
No confiar en el frontend como fuente final de autorización.  
Ocultar o mostrar opciones mejora la UX, pero la verificación decisiva debe ocurrir en el backend.

---

## 8. Enfoque sugerido por etapa

## 8.1 Etapa actual
- incluir `roles`,
- mantener el token corto,
- usar roles en frontend para navegación básica,
- y resolver permisos efectivos en backend.

## 8.2 Etapa siguiente
Si la UI lo necesita:
- agregar `capabilities` resumidas,
- o exponer un endpoint de autorización efectiva.

## 8.3 Etapa avanzada
Si el producto crece mucho en personalización:
- evaluar permisos más dinámicos,
- pero sin transformar el JWT en una matriz completa de seguridad.

---

## 9. Ejemplo conceptual de access token recomendado

```json
{
  "iss": "https://auth.keygo.cl",
  "sub": "usr_123",
  "aud": "keygo-ui-api",
  "sid": "sess_456",
  "tenant_id": "tnt_789",
  "roles": ["KEYGO_ACCOUNT_ADMIN"],
  "capabilities": [
    "profile:self",
    "sessions:self",
    "activity:self",
    "users:manage",
    "apps:manage",
    "billing:read"
  ]
}
```

### Observación
El claim `capabilities` es opcional.  
Si no hace falta, se puede prescindir de él y dejar solo `roles`.

---

## 10. Conclusión

La postura recomendada para Keygo es:

- **no** inflar el JWT con permisos finos,
- **sí** incluir roles,
- **sí** usar capacidades resumidas solo si agregan valor real,
- **sí** resolver la autorización efectiva en backend.

La regla práctica de proyecto sería:

> **Token pequeño, autorización fuerte en servidor.**
