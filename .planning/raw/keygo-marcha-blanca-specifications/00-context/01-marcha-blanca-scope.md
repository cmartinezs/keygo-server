# Alcance de marcha blanca controlada KeyGo

| Campo | Valor |
|---|---|
| Tipo | Product / Scope specification |
| Prioridad | P0 contextual |
| Área | Frontend y Backend |
| Estado objetivo | Piloto guiado, no autoservicio público |

## Objetivo

Definir el alcance mínimo y seguro para habilitar una primera marcha blanca de KeyGo sin exponer flujos incompletos o inconsistentes a clientes reales.

## Alcance permitido

Durante la marcha blanca se permite:

- 1 tenant interno o piloto.
- 1 usuario `KEYGO_ADMIN`.
- 1 usuario `KEYGO_ACCOUNT_ADMIN`.
- 1 app `PUBLIC` con Authorization Code + PKCE.
- 1 app `CONFIDENTIAL` para probar client credentials, si aplica.
- Menos de 20 usuarios.
- Menos de 20 apps.
- Registro de usuarios solo por admin.
- Memberships creadas manualmente por tenant admin.
- Política default de app: `CLOSED` o `INVITE_ONLY`.

## Fuera de alcance para marcha blanca

No habilitar:

- Registro público completo.
- Auto-join abierto.
- Solicitud pública de acceso sin aprobación.
- Billing real.
- Tenants externos autogestionados.
- Administración masiva.
- Dependencia de mocks en login, apps, usuarios, memberships, roles o logout.

## Reglas de producto

1. Un usuario pertenece al tenant.
2. Una app pertenece al tenant.
3. El acceso de un usuario a una app se modela mediante membership.
4. Los roles de app se asignan mediante membership.
5. Las apps no deben recibir tokens si el usuario no tiene membership activa cuando la política de la app es cerrada.

## Criterio de salida mínima

```text
GO si:
- crear app muestra secret
- app permite redirect URIs/scopes
- tenant admin puede entrar sin 403 por rol
- membership muestra app + roles legibles + fecha válida
- usuario puede login y recibir token con roles correctos
- app externa puede validar acceso por membership
```

```text
NO-GO si:
- el secret se pierde
- tenant admin no pasa autorización backend
- memberships muestran UUIDs/fechas inválidas
- hay mocks activos en flujos centrales
- no puedes configurar roles de app desde UI
```

## Instrucción para AI Agent

Antes de implementar cualquier funcionalidad nueva, verificar que no contradiga este alcance. Si un cambio tiende a habilitar autoservicio público, auto-join o flujo abierto de solicitud de acceso, dejarlo deshabilitado por defecto y documentado como pendiente.
