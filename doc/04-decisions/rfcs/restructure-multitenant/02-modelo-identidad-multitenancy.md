# 02. Modelo de identidad, multitenancy y aplicaciones

## Objetivo del modelo

Definir una separación limpia entre:

- la identidad base del usuario,
- su presencia en un tenant,
- y su acceso a una aplicación de ese tenant.

---

## 1. Capas conceptuales del modelo

## 1.1 Cuenta global de plataforma

Representa a la persona dentro de Keygo como plataforma.  
Es la entidad responsable de:

- email,
- credenciales,
- MFA,
- recuperación de contraseña,
- sesiones globales,
- configuración de cuenta,
- actividad personal,
- perfil.

Esta cuenta existe aunque la persona todavía no esté asignada a una app concreta.

### Qué no representa
No representa, por sí sola:

- pertenencia a un tenant,
- permiso de administración de un tenant,
- derecho a usar una app cliente.

---

## 1.2 Usuario en tenant

Representa la participación de una cuenta global dentro de un tenant específico.

Este nivel existe para responder preguntas como:

- ¿esta persona pertenece al tenant A?
- ¿está activa, invitada o suspendida dentro del tenant?
- ¿tiene rol organizacional dentro del tenant?
- ¿puede aparecer en la administración de usuarios del tenant?

### Qué resuelve
Resuelve el vínculo entre una persona y una organización.

### Qué no resuelve
No determina automáticamente a qué apps puede entrar.

---

## 1.3 Membership o asignación a app

Representa que una persona, ya existente dentro de un tenant, puede usar una app concreta de ese tenant.

Este es el nivel adecuado para modelar:

- acceso a la app,
- estado de acceso,
- roles por app,
- restricciones de activación,
- e incluso futuros consentimientos o suscripciones.

---

## 2. Principio central propuesto

La propuesta recomienda el siguiente principio:

> **La identidad es global, la pertenencia es por tenant y el acceso es por app.**

Esta frase resume el modelo recomendado para Keygo.

---

## 3. Qué evita este enfoque

Este diseño evita varios errores habituales:

### A. Duplicar usuarios por app
Un mismo usuario no necesita existir varias veces solo porque usa más de una app del mismo tenant.

### B. Convertir un tenant en pseudo-plataforma
No hace falta que todos los usuarios “pertenezcan” al tenant `keygo` para poder tener perfil o sesiones.

### C. Dar acceso implícito a todas las apps
Pertenecer al tenant no implica automáticamente pertenecer a todas sus aplicaciones.

### D. Mezclar autenticación con autorización
La cuenta global permite autenticarse. La membership a app define si puede usar una app particular.

---

## 4. Interpretación del tenant interno `keygo`

## 4.1 Qué sí es
Un tenant reservado de uso interno de la plataforma.

Puede servir para:

- operación interna,
- uso dogfooding,
- apps propias de Keygo,
- administración del equipo interno.

## 4.2 Qué no es
No debería ser el sustituto del concepto “cuenta global”.

Si se usa así, el modelo se contamina y el tenant deja de representar una organización para pasar a representar un concepto transversal de plataforma.

---

## 5. Políticas de ingreso a apps

La asignación a una app puede resolverse con políticas diferentes. Se recomienda que esto sea configurable por app.

## 5.1 Closed app
Solo entra quien ya tenga membership activa.

### Ventajas
- más seguro,
- más predecible,
- más compatible con SaaS B2B.

### Recomendación
Tomarlo como valor por defecto del MVP.

## 5.2 Open join
Si el usuario ya pertenece al tenant, al primer acceso se crea automáticamente su membership en la app.

### Ventajas
- reduce fricción,
- útil para adopción dentro de equipos pequeños.

### Riesgos
- puede abrir acceso de más si no se controla bien.

## 5.3 Self-signup
El usuario se crea y queda asignado a la app mediante flujo abierto o semiconfigurado.

### Recomendación
No priorizarlo como camino principal del MVP B2B.

---

## 6. Implicancias funcionales

Con este modelo, el sistema puede responder correctamente estas situaciones:

### Caso 1
Una persona tiene cuenta global y pertenece al tenant A, pero no a ninguna app.
- Puede autenticarse en Keygo.
- Puede ver su cuenta personal.
- No puede usar apps del tenant hasta tener membership.

### Caso 2
Una persona pertenece al tenant A y a dos apps del tenant.
- Usa una sola identidad.
- Tiene acceso diferenciado por app.
- Puede tener roles distintos en cada una.

### Caso 3
La misma persona participa en más de un tenant.
- Mantiene una misma cuenta global.
- Tiene presencias separadas por tenant.
- Sus roles y accesos se resuelven por contexto.

---

## 7. Entidades conceptuales mínimas

No se listan todos los campos, porque el objetivo de este documento es conceptual, pero las entidades mínimas recomendadas son:

- `platform_user`
- `tenant`
- `tenant_user`
- `client_app`
- `app_membership`

Y, para autorización:
- `platform_role`
- `tenant_role`
- `app_role`

---

## 8. Recomendación de naming

Para que el modelo se entienda mejor, conviene usar nombres que expresen el contexto:

- `platform_user`: identidad base,
- `tenant_user`: presencia dentro de una organización,
- `app_membership`: acceso a una aplicación.

Esto ayuda a que la semántica del sistema se sostenga con el tiempo y reduzca confusiones al implementar.

---

## 9. Decisión recomendada para Keygo

La postura recomendada es:

- **sí** a una cuenta global,
- **sí** a presencia por tenant,
- **sí** a acceso por app,
- **no** a considerar que todo usuario del sistema sea miembro del tenant interno `keygo`.

Esta decisión deja a Keygo preparado para crecer en complejidad sin deformar su base conceptual.
