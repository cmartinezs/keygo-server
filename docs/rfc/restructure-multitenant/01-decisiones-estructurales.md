# 01. Decisiones estructurales del producto

## Resumen ejecutivo de decisiones

Las decisiones principales propuestas para Keygo son las siguientes:

1. **La cuenta global del usuario no debe confundirse con la pertenencia a un tenant.**
2. **No todo usuario del sistema debe ser modelado como miembro del tenant interno `keygo`.**
3. **El acceso a apps debe resolverse a través de memberships o asignaciones específicas.**
4. **`keygo-ui` debe entenderse como una sola superficie web física, pero con varias superficies lógicas.**
5. **El hosted login debe depender de la sesión global de Keygo, no de una sesión local de la app cliente.**
6. **Los roles propios de Keygo deben separarse del RBAC de tenant y del RBAC de app.**
7. **El JWT no debe inflarse con permisos finos; debe priorizar roles y, como máximo, capacidades resumidas.**

---

## 1. Cuenta global vs pertenencia a tenant

### Decisión
La identidad base del usuario debe vivir a nivel plataforma y no ser equivalente a una membresía dentro de un tenant.

### Razonamiento
Una persona puede:

- tener una cuenta de plataforma,
- pertenecer a uno o más tenants,
- acceder a una o varias apps por tenant,
- y eventualmente no pertenecer al tenant interno `keygo` aunque siga siendo usuario de la plataforma.

### Beneficio
Esto separa correctamente:

- identidad,
- organización,
- y acceso funcional.

---

## 2. Tenant interno `keygo`

### Decisión
`keygo` puede existir como tenant reservado, pero no debe usarse para modelar universalmente a todos los usuarios del ecosistema.

### Razonamiento
Usar el tenant `keygo` como “contenedor universal” de toda cuenta global genera una ficción de dominio: mezcla una organización interna real con una abstracción de plataforma.

### Uso válido del tenant interno `keygo`
Sí es válido que `keygo` exista como tenant reservado para:

- operar apps internas,
- probar el producto sobre sí mismo,
- administrar usuarios internos del equipo,
- o usar parte de la misma console con datos de Keygo.

### Uso que se desaconseja
No se recomienda decir que “todo usuario del sistema es miembro de `keygo`” solo porque necesita perfil, sesiones o actividad.

---

## 3. Acceso a aplicaciones

### Decisión
El acceso a una app no debe depender solo del tenant ni solo de la cuenta global. Debe resolverse con una asignación explícita a la app.

### Razonamiento
Esto permite:

- que un usuario use múltiples apps del mismo tenant sin duplicarse,
- que no todas las personas del tenant tengan acceso automático a todas las apps,
- y que los permisos por app sean modelables de forma limpia.

### Consecuencia
La unidad de acceso real deja de ser “usuario” a secas y pasa a ser “usuario en contexto de app”.

---

## 4. `keygo-ui` como superficie web

### Decisión
Se puede mantener un único proyecto frontend por ahora, pero debe separarse en superficies funcionales.

### Razonamiento
Desde despliegue puede ser una sola SPA, pero desde dominio no debe mezclarse:

- landing pública,
- hosted login,
- portal de cuenta personal,
- console de tenant,
- ops de plataforma.

### Principio
Un solo proyecto físico no implica una sola responsabilidad lógica.

---

## 5. Hosted login como patrón principal

### Decisión
Si una app cliente no quiere implementar login propio, debe delegarlo a Keygo mediante hosted login.

### Razonamiento
La app cliente no necesita mostrar formulario propio si puede redirigir a Keygo para iniciar el flujo OIDC/OAuth2.

### Beneficio
Se centraliza:

- login,
- recuperación de credenciales,
- seguridad,
- MFA futuro,
- y experiencia de SSO.

---

## 6. RBAC de Keygo separado del resto

### Decisión
Los roles de Keygo deben vivir en un ámbito propio, distinto del RBAC de tenant y distinto del RBAC de app.

### Razonamiento
Los roles del producto Keygo responden a la administración del propio servicio, no a la administración funcional de los clientes.

### Implicancia
Se recomienda distinguir al menos tres universos de autorización:

1. RBAC de Keygo,
2. RBAC del tenant cliente,
3. RBAC de cada app.

---

## 7. Claims del token

### Decisión
El JWT debe contener identidad, contexto y roles. No debe convertirse en un contenedor de permisos finos.

### Razonamiento
Los permisos finos hacen crecer el token, endurecen el acoplamiento y vuelven más sensible el cambio de autorización.

### Principio recomendado
- roles en el token,
- permisos efectivos resueltos en backend,
- capacidades resumidas solo si la UI realmente las necesita.

---

## 8. Nivel de ambición adecuado para la etapa actual

Esta propuesta no busca sobrediseñar el sistema, sino dejar una base ordenada para que Keygo pueda:

- empezar simple,
- entregar valor rápido,
- y crecer sin contradicciones fuertes.

Por eso varias recomendaciones se formulan en dos capas:

- **implementación recomendable ahora**, y
- **evolución preparada para después**.
