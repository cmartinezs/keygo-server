# 05. RBAC de Keygo

## Objetivo

Definir cómo debe modelarse la autorización propia de Keygo, separada del RBAC de cada tenant cliente y del RBAC de cada app cliente.

---

## 1. Alcance de este documento

Este documento trata solo el **RBAC interno de Keygo**.  
No cubre:

- los roles de cada tenant cliente,
- ni los roles específicos de cada aplicación registrada.

Esto es importante porque, si se mezclan los tres ámbitos, la semántica del sistema se vuelve frágil.

---

## 2. Principio recomendado

Keygo debe tener su propio universo de roles, permisos y evaluación de acceso para:

- console del producto,
- administración de cuenta,
- administración de tenant contratado,
- y operaciones globales de plataforma.

---

## 3. Roles base recomendados

La conversación funcional llevó a tres niveles jerárquicos de rol para Keygo:

- administrador global del producto,
- administrador del espacio contratado dentro de Keygo,
- usuario final del propio Keygo.

Para evitar confusiones con los roles de tenant cliente, se recomienda renombrarlos explícitamente.

### Propuesta de naming
- `KEYGO_ADMIN`
- `KEYGO_ACCOUNT_ADMIN`
- `KEYGO_USER`

---

## 4. Semántica de cada rol

## 4.1 `KEYGO_USER`

Representa al usuario base de Keygo como producto.

### Capacidades principales
- ver su perfil,
- actualizar datos de cuenta permitidos,
- ver sus sesiones,
- cerrar sus sesiones,
- revisar su actividad,
- y usar superficies personales del sistema.

---

## 4.2 `KEYGO_ACCOUNT_ADMIN`

Representa al usuario que administra el espacio contratado dentro de Keygo.

### Capacidades principales
Incluye todo lo de `KEYGO_USER`, más:
- administrar el tenant contratado dentro de Keygo,
- gestionar apps,
- gestionar usuarios,
- gestionar memberships,
- revisar o administrar billing según política,
- configurar parámetros funcionales del tenant en Keygo.

---

## 4.3 `KEYGO_ADMIN`

Representa al operador global de la plataforma Keygo.

### Capacidades principales
Incluye todo lo de `KEYGO_ACCOUNT_ADMIN`, más:
- administración global del producto,
- gestión global de tenants,
- soporte operativo,
- observabilidad administrativa,
- configuraciones globales,
- y acciones especiales de mantenimiento o soporte.

---

## 5. Jerarquía propuesta

Se propone esta jerarquía funcional:

```text
KEYGO_ADMIN
  -> KEYGO_ACCOUNT_ADMIN
      -> KEYGO_USER
```

Esto significa que el rol superior **implica** las capacidades del inferior, pero no necesariamente que “sea lo mismo”.

---

## 6. Recomendación de autorización

No se recomienda repartir lógica por todo el código del tipo:

- `if role == ADMIN`
- `if role == TENANT_ADMIN`

En su lugar, se recomienda:

- roles bien definidos,
- permisos o capacidades asociadas a rol,
- evaluación centralizada,
- y una jerarquía explícita.

---

## 7. Dos formas válidas de implementarlo

## 7.1 Forma simple para MVP

Usar un `enum` con nivel jerárquico, por ejemplo:

- `KEYGO_USER = 1`
- `KEYGO_ACCOUNT_ADMIN = 2`
- `KEYGO_ADMIN = 3`

Y evaluar herencia por nivel.

### Cuándo sirve
- pocos roles,
- jerarquía estable,
- autorización todavía simple.

### Ventajas
- implementación rápida,
- fácil de entender,
- bajo costo inicial.

---

## 7.2 Forma más extensible

Usar un modelo más declarativo con:

- `role`
- `permission`
- `role_permission`
- opcionalmente `role_implication`
- y `role_assignment`

### Cuándo conviene
- cuando empiecen a aparecer permisos finos,
- cuando algunos administradores no deban tener todo,
- o cuando la plataforma necesite mayor flexibilidad.

---

## 8. Capacidades sugeridas por rol

## 8.1 Capacidades base de `KEYGO_USER`
- `profile:self.read`
- `profile:self.update`
- `sessions:self.read`
- `sessions:self.revoke`
- `activity:self.read`

## 8.2 Capacidades base de `KEYGO_ACCOUNT_ADMIN`
Además de las anteriores:
- `tenant:read`
- `tenant:update`
- `users:read`
- `users:manage`
- `apps:read`
- `apps:manage`
- `billing:read`
- `billing:manage` o una variante más acotada, según política comercial

## 8.3 Capacidades base de `KEYGO_ADMIN`
Además de las anteriores:
- `platform:manage`
- `tenants:read.all`
- `tenants:manage.all`
- `support:manage`
- `audit:read.all`

---

## 9. Principio de separación de ámbitos

Debe quedar explícito en la documentación del sistema que:

- el RBAC de Keygo administra el producto Keygo,
- el RBAC de tenant administra una organización cliente,
- y el RBAC de app administra permisos dentro de una app concreta.

Esto evita que, con el tiempo, el nombre de un rol termine usándose para tres contextos distintos.

---

## 10. Decisión recomendada para el proyecto

Para la etapa actual se recomienda:

- partir con un conjunto pequeño de roles,
- nombrarlos explícitamente como roles de Keygo,
- resolver herencia de forma clara,
- y dejar preparada una evolución hacia permisos/capacidades si el producto lo necesita.

La postura concreta sugerida es:

- **sí** a jerarquía simple,
- **sí** a naming explícito,
- **sí** a autorización centralizada,
- **no** a mezclar estos roles con los de tenant o app.
