# 08. Diagramas Mermaid

## Objetivo

Reunir los diagramas base para explicar la propuesta en presentaciones, README técnicos o documentación interna.

---

## 1. Modelo conceptual principal

```mermaid
erDiagram

    PLATFORM_USER ||--o{ TENANT_USER : participa_como
    TENANT ||--o{ TENANT_USER : contiene

    TENANT ||--o{ CLIENT_APP : registra

    TENANT_USER ||--o{ APP_MEMBERSHIP : accede_a
    CLIENT_APP ||--o{ APP_MEMBERSHIP : habilita
```

### Lectura
- `PLATFORM_USER` es la cuenta global.
- `TENANT_USER` representa la presencia dentro de una organización.
- `APP_MEMBERSHIP` representa el acceso efectivo a una app.

---

## 2. Modelo extendido con roles por ámbito

```mermaid
erDiagram

    PLATFORM_USER ||--o{ PLATFORM_USER_ROLE : tiene
    PLATFORM_ROLE ||--o{ PLATFORM_USER_ROLE : asigna

    PLATFORM_USER ||--o{ TENANT_USER : participa_en
    TENANT ||--o{ TENANT_USER : contiene

    TENANT ||--o{ TENANT_ROLE : define
    TENANT_USER ||--o{ TENANT_USER_ROLE : tiene
    TENANT_ROLE ||--o{ TENANT_USER_ROLE : asigna

    TENANT ||--o{ CLIENT_APP : registra

    TENANT_USER ||--o{ APP_MEMBERSHIP : accede_a
    CLIENT_APP ||--o{ APP_MEMBERSHIP : habilita

    CLIENT_APP ||--o{ APP_ROLE : define
    APP_MEMBERSHIP ||--o{ APP_MEMBERSHIP_ROLE : tiene
    APP_ROLE ||--o{ APP_MEMBERSHIP_ROLE : asigna
```

### Lectura
Este diagrama muestra tres ámbitos de autorización:

1. plataforma,
2. tenant,
3. app.

---

## 3. Superficies lógicas de `keygo-ui`

```mermaid
flowchart TD
    A[Keygo UI] --> B[Landing pública]
    A --> C[Hosted Auth]
    A --> D[Portal de cuenta]
    A --> E[Console de tenant]
    A --> F[Ops de plataforma]
```

### Lectura
Hay una sola superficie web física, pero varias superficies lógicas claramente diferenciadas.

---

## 4. Flujo de autenticación con hosted login

```mermaid
sequenceDiagram
    participant U as Usuario
    participant APP as App cliente
    participant KG as Keygo Hosted Login
    participant API as Token Endpoint

    U->>APP: Intenta acceder
    APP->>KG: Redirección a /authorize
    KG->>KG: Revisa sesión global
    alt No existe sesión global
        KG->>U: Muestra login
        U->>KG: Envía credenciales
    end
    KG->>KG: Valida acceso a la app
    KG-->>APP: Authorization Code
    APP->>API: Intercambia code por tokens
    API-->>APP: Access Token / ID Token
```

### Lectura
La app cliente delega autenticación en Keygo.  
La sesión global puede reutilizarse, pero la app mantiene su propio contexto de acceso.

---

## 5. Separación entre sesión global y sesión de app

```mermaid
flowchart LR
    A[Sesión global Keygo] --> B[Hosted Login]
    A --> C[Portal de cuenta]
    A --> D[Console]
    A -. SSO .-> E[Acceso a app cliente]

    E --> F[Sesión propia de app]
```

### Lectura
La sesión global habilita SSO, pero no reemplaza la sesión local de cada app.

---

## 6. Jerarquía de roles de Keygo

```mermaid
flowchart TD
    A[KEYGO_ADMIN] --> B[KEYGO_ACCOUNT_ADMIN]
    B --> C[KEYGO_USER]
```

### Lectura
El rol superior implica las capacidades del inferior.

---

## 7. Relación recomendada entre roles y autorización

```mermaid
flowchart LR
    A[Roles en JWT] --> B[Expansión de capacidades]
    B --> C[Autorización backend]
    B --> D[Renderizado de UI]
```

### Lectura
El JWT entrega información compacta; la expansión a permisos efectivos se resuelve fuera del token.

---

## 8. Arquitectura conceptual de frontend

```mermaid
flowchart TD
    A[src/features] --> B[public]
    A --> C[auth]
    A --> D[account]
    A --> E[console]
    A --> F[ops]
```

### Lectura
Una única aplicación frontend puede mantenerse ordenada si se estructura por superficies funcionales.

---

## 9. Idea central resumida

```mermaid
flowchart TD
    A[Cuenta global] --> B[Participación en tenant]
    B --> C[Acceso a apps]
```

### Lectura
Esta es la síntesis del modelo recomendado para Keygo.
