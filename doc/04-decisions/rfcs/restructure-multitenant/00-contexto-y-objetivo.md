# 00. Contexto y objetivo

## Contexto

Keygo se está consolidando como un sistema IAM multitenant orientado a ofrecer autenticación, administración de usuarios y control de acceso para múltiples clientes, cada uno con su propio tenant y con múltiples aplicaciones asociadas.

En la evolución del proyecto aparecieron varias preguntas estructurales que impactan directamente el diseño del sistema:

- si la identidad del usuario debe ser única por tenant o global para toda la plataforma,
- cómo debe entenderse el tenant interno `keygo`,
- qué responsabilidad real cumple `keygo-ui`,
- cómo se reutiliza el login hosted desde las apps cliente,
- cómo modelar los roles propios de Keygo,
- y qué información conviene incluir dentro del JWT.

Estas definiciones no son detalles menores. Determinan el modelo de datos, la UX, la seguridad, la trazabilidad y la capacidad de crecer sin entrar en contradicciones.

## Objetivo de la propuesta

El objetivo de esta documentación es dejar una postura de proyecto clara y defendible respecto de:

1. **modelo de identidad**,
2. **modelo de pertenencia a tenant**,
3. **modelo de acceso a apps**,
4. **responsabilidades de la UI**,
5. **RBAC de Keygo**,
6. **estrategia de claims y autorización**.

## Preguntas que esta documentación responde

### A nivel de dominio
- ¿Todo usuario pertenece realmente al tenant interno `keygo`?
- ¿La cuenta global de la plataforma es lo mismo que la membresía a un tenant?
- ¿El acceso a apps se modela por usuario, por tenant o por membership?

### A nivel de UX y frontend
- ¿`keygo-ui` es una sola aplicación o varias superficies lógicas?
- ¿La landing, el login, el portal de cuenta y la console deben separarse?
- ¿Es válido mantener todo en un mismo proyecto React + Vite?

### A nivel de seguridad
- ¿Los roles de Keygo deben mezclarse con los roles de tenant y app?
- ¿Conviene enviar permisos finos en el JWT?
- ¿Cómo soportar SSO sin mezclar sesión global con sesión de app?

## Resultado conceptual esperado

La propuesta busca que Keygo quede modelado como una plataforma con las siguientes propiedades:

- una **cuenta global** para identidad, credenciales y perfil,
- una **representación por tenant** para participación organizacional,
- una **asignación por app** para autorización funcional,
- una **UI separada lógicamente** por superficies,
- un **RBAC propio de Keygo** independiente del RBAC de tenant y app,
- un **JWT controlado en tamaño y semántica**.

## Criterios usados en esta propuesta

Las decisiones aquí presentadas se apoyan en los siguientes criterios:

### 1. Coherencia de dominio
Evitar que una misma entidad represente cosas distintas según el contexto.

### 2. Escalabilidad funcional
Permitir crecer a nuevos casos sin tener que romper el modelo base.

### 3. Aislamiento entre tenants
Mantener una frontera clara entre organizaciones clientes.

### 4. Evolución segura del producto
Permitir que el MVP sea simple, pero sin hipotecar el diseño futuro.

### 5. Claridad de implementación
Reducir los “atajos” que después obligan a introducir excepciones por todas partes.
