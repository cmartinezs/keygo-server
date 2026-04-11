# PROJECT_STRUCTURE (Archived)

⚠️ **This document is archived and no longer maintained.**

Project structure is now documented in:
- [`../../design/ARCHITECTURE.md`](../../design/ARCHITECTURE.md) — Complete architecture overview
- [`../../README.md`](../../README.md) — Repository structure

Archived in: [`../../archive/deprecated/`](../../archive/deprecated/)
- `keygo-app`
- `keygo-bom`
- `keygo-common`
- `keygo-domain`
- `keygo-infra`
- `keygo-run`
- `keygo-supabase`

Además existen carpetas y archivos de soporte como:

- `.github`
- `docs`
- `scripts`
- `docker-compose.yml`
- `pom.xml`
- `README.md`

La dirección general es buena: ya existe intención de separar dominio, aplicación, infraestructura y arranque.

---

## 3. Evaluación general

## 3.1. Lo que está bien

### Separación por módulos
Tener `domain`, `app`, `api`, `infra` y `run` ya marca una buena base.

### BOM separado
`keygo-bom` es una buena decisión si quieres controlar versiones de dependencias y mantener consistencia.

### Módulo de arranque separado
`keygo-run` como punto de entrada es correcto. Permite que el resto del sistema no dependa del bootstrap.

### Aislamiento de integración específica
Tener `keygo-supabase` separado del resto también es una buena señal: estás encapsulando una integración concreta.

---

## 3.2. Riesgos actuales

### Riesgo 1: nombres demasiado genéricos dentro de módulos
Paquetes como:
- `helper`
- `constant`
- `mapper`
- `filter`
- `config`

son útiles al inicio, pero tienden a crecer sin límite y mezclar responsabilidades.

### Riesgo 2: `keygo-api` puede terminar absorbiendo demasiada lógica
Si controllers, filtros, excepciones, DTOs, mappers y helpers crecen sin criterio, el módulo API se convierte en un “mini monolito sucio”.

### Riesgo 3: `keygo-supabase` parece mezclar persistencia + migraciones + configuración específica
Eso puede estar bien mientras el módulo represente un adapter concreto, pero hay que cuidar que no se vuelva “la infraestructura real” mientras `keygo-infra` queda vacío o ambiguo.

### Riesgo 4: `keygo-common` puede convertirse en un basurero
Los módulos `common` suelen degenerar si no tienen reglas estrictas.

---

## 4. Estructura objetivo recomendada

La recomendación es mantener los módulos actuales, pero definir mejor su responsabilidad.

## 4.1. Módulos

### `keygo-domain`
Debe contener exclusivamente:
- entidades de dominio,
- value objects,
- enums de dominio,
- reglas de negocio puras,
- eventos de dominio,
- servicios de dominio puros,
- excepciones de dominio.

**No debe contener**:
- anotaciones Spring,
- JPA,
- DTOs HTTP,
- repositorios JPA,
- clases utilitarias técnicas.

---

### `keygo-app`
Debe contener:
- casos de uso,
- puertos de entrada,
- puertos de salida,
- comandos y queries,
- respuestas de aplicación,
- orquestación de negocio.

Aquí es donde viven los interactors/use cases.

**Debe depender de `keygo-domain`, pero no de Spring Web ni de JPA.**

---

### `keygo-api`
Debe contener solo la capa de entrega HTTP:
- controllers,
- request/response DTOs,
- mappers HTTP ↔ aplicación,
- manejo de errores HTTP,
- validaciones de entrada,
- filtros estrictamente web.

Aquí no deberían existir reglas de negocio importantes.

---

### `keygo-infra`
Debe representar infraestructura transversal no atada necesariamente a un proveedor específico.

Ejemplos:
- jwt signing service,
- hashing service,
- clock provider,
- id generator,
- auditoría,
- envío de correos,
- soporte a cache,
- componentes técnicos reutilizables.

---

### `keygo-supabase`
Debe existir solo si realmente representa un adapter o integración concreta.

La recomendación es tratarlo como:

**adapter de persistencia / integración específica de Supabase/Postgres**

Entonces debería contener:
- entidades JPA si realmente las necesitas ahí,
- repositories concretos,
- mappers dominio ↔ persistencia,
- configuración propia del datasource si es exclusiva,
- migraciones si ese módulo es efectivamente el adapter persistente.

Si mañana cambias de proveedor, este módulo debería ser reemplazable.

---

### `keygo-run`
Debe ser el bootstrap:
- `@SpringBootApplication`
- wiring principal
- configuración runtime
- profile selection
- arranque de contexto

No debería contener lógica de negocio.

---

### `keygo-common`
Debe mantenerse muy pequeño.

Solo debería contener piezas realmente transversales y estables, por ejemplo:
- utilidades de bajo nivel muy acotadas,
- tipos compartidos que no pertenecen a dominio ni aplicación,
- convenciones técnicas comunes.

**Regla fuerte:** si dudas si algo va en `common`, probablemente no debería ir ahí.

---

## 5. Estructura interna recomendada por módulo

## 5.1. `keygo-domain`

Recomendación:

```text
keygo-domain
└── src/main/java/io/cmartinezs/keygo/domain
    ├── tenant
    │   ├── model
    │   ├── event
    │   └── exception
    ├── clientapp
    │   ├── model
    │   ├── event
    │   └── exception
    ├── user
    │   ├── model
    │   ├── event
    │   └── exception
    ├── membership
    │   ├── model
    │   ├── event
    │   └── exception
    ├── auth
    │   ├── model
    │   ├── event
    │   └── exception
    └── shared
        ├── valueobject
        └── exception
```

### Recomendación clave
Organiza por **subdominio / feature**, no por “entity”, “service”, “repository” globales.

---

## 5.2. `keygo-app`

Recomendación:

```text
keygo-app
└── src/main/java/io/cmartinezs/keygo/app
    ├── port
    │   ├── in
    │   └── out
    ├── tenant
    │   ├── command
    │   ├── query
    │   ├── usecase
    │   └── result
    ├── clientapp
    │   ├── command
    │   ├── query
    │   ├── usecase
    │   └── result
    ├── user
    │   ├── command
    │   ├── query
    │   ├── usecase
    │   └── result
    ├── membership
    │   ├── command
    │   ├── query
    │   ├── usecase
    │   └── result
    ├── auth
    │   ├── command
    │   ├── query
    │   ├── usecase
    │   └── result
    └── shared
```

### Recomendación clave
Evita dejar todo solo en:
- `port.out`
- `usecase`

porque crecerá demasiado rápido.

Mejor agrupar por feature y dentro de cada feature sus comandos, resultados y casos de uso.

---

## 5.3. `keygo-api`

Tu estructura actual ya tiene:
- `config`
- `constant`
- `controller`
- `dto`
- `exception`
- `filter`
- `helper`
- `mapper`

La recomendación es evolucionarla a algo más orientado a plano + feature:

```text
keygo-api
└── src/main/java/io/cmartinezs/keygo/api
    ├── auth
    │   ├── controller
    │   ├── request
    │   ├── response
    │   └── mapper
    ├── tenantadmin
    │   ├── controller
    │   ├── request
    │   ├── response
    │   └── mapper
    ├── platform
    │   ├── controller
    │   ├── request
    │   ├── response
    │   └── mapper
    ├── account
    │   ├── controller
    │   ├── request
    │   ├── response
    │   └── mapper
    ├── config
    ├── error
    ├── security
    │   ├── filter
    │   ├── resolver
    │   └── principal
    └── shared
```

### Recomendaciones concretas sobre tu estructura actual

#### `constant`
Evítalo salvo para constantes técnicas muy puntuales.
Muchas constantes deberían vivir como:
- enums de dominio,
- propiedades de configuración,
- o clases de contrato específicas.

#### `helper`
Recomiendo eliminar esta carpeta gradualmente.
El nombre no comunica responsabilidad. Si algo merece existir, debería tener un nombre de intención real.

#### `filter`
Muévelo bajo `security/filter` o `web/filter` según corresponda.

#### `dto`
Sepáralo en `request` y `response`, idealmente por feature.

#### `mapper`
Mantén mappers, pero por feature, no globales.

---

## 5.4. `keygo-supabase`

Tu estructura actual parece:
- `config`
- `entity`
- `repository`
- `resources/db.migration`

Recomendación objetivo:

```text
keygo-supabase
└── src/main/java/io/cmartinezs/keygo/supabase
    ├── config
    ├── persistence
    │   ├── tenant
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── mapper
    │   ├── clientapp
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── mapper
    │   ├── user
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── mapper
    │   ├── membership
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── mapper
    │   └── auth
    │       ├── entity
    │       ├── repository
    │       └── mapper
    └── support
```

### Razón
No conviene una sola carpeta global `entity` ni una sola carpeta global `repository` cuando el modelo crece.

Agrupar por feature hace mucho más mantenible el módulo persistente.

---

## 5.5. `keygo-run`

Tu estructura actual va bien.

Recomendación:

```text
keygo-run
└── src/main/java/io/cmartinezs/keygo/run
    ├── KeygoRunner.java
    ├── config
    ├── bootstrap
    └── support
```

### Recomendación concreta
- `filter` no debería vivir aquí salvo que sea puramente de bootstrapping.
- Si es filtro web/security, mejor dejarlo en `keygo-api`.

---

## 6. Organización por feature vs por capa

Tu proyecto ya está organizado por módulo/capa a alto nivel. Eso está bien.

Pero **dentro** de los módulos recomiendo fuertemente organizar por **feature/subdominio**.

### Correcto
- módulo = capa macro
- paquete interno = feature

### Menos recomendable a mediano plazo
- módulo = capa macro
- paquete interno = tipos técnicos genéricos globales (`controller`, `service`, `mapper`, `entity`)

La combinación ideal para tu caso es:

- **por capas entre módulos**,
- **por feature dentro de cada módulo**.

---

## 7. Dependencias entre módulos recomendadas

La dirección recomendable es:

```text
keygo-domain
    ↑
keygo-app
    ↑
keygo-api       keygo-infra       keygo-supabase
         \         |              /
                 keygo-run
```

### Reglas
- `keygo-domain` no depende de nadie.
- `keygo-app` depende de `keygo-domain`.
- `keygo-api` depende de `keygo-app` y, si hace falta, de contratos compartidos, pero no de repositorios concretos.
- `keygo-supabase` implementa puertos de salida definidos en `keygo-app`.
- `keygo-run` ensambla todo.

### Regla importante
Evita que `keygo-api` hable directamente con `repository` de `keygo-supabase`.
Eso rompe la arquitectura limpia.

---

## 8. Nombres de clases recomendados

## 8.1. Use cases
Usa nombres de intención clara:
- `CreateTenantUseCase`
- `CreateClientAppUseCase`
- `AuthenticateUserUseCase`
- `ExchangeAuthorizationCodeUseCase`
- `CreateMembershipUseCase`

## 8.2. Puertos de entrada
- `CreateTenantHandler`
- `CreateClientAppHandler`
- `AuthenticateUserHandler`

O si prefieres interfaz + implementación:
- `CreateTenantUseCase`
- `CreateTenantService`

Pero sé consistente.

## 8.3. Puertos de salida
- `TenantRepositoryPort`
- `ClientAppRepositoryPort`
- `PasswordHasherPort`
- `TokenSignerPort`
- `AuditPublisherPort`

## 8.4. Controllers
- `AuthorizationController`
- `TenantAdminAppController`
- `TenantAdminUserController`
- `AccountController`
- `PlatformTenantController`

Evita `MainController`, `GenericController`, etc.

---

## 9. Recomendaciones específicas sobre tu estructura actual

## 9.1. Renombrar `keygo-run/KeyGoRunner`
Usaría:
- `KeygoApplication`

porque sigue la convención más reconocible de Spring Boot.

## 9.2. Cuidar `keygo-supabase`
Si el módulo realmente es Postgres/Supabase persistence, documenta explícitamente su rol.

Si no, el nombre puede inducir a acoplar el proyecto completo a Supabase más de la cuenta.

Alternativa futura si quieres neutralidad:
- `keygo-persistence-postgres`
- `keygo-adapter-persistence-postgres`

Pero por ahora puedes dejarlo como está si Supabase es parte real del stack inicial.

## 9.3. Mantener `docs`
Muy buena decisión.
Recomiendo guardar ahí los archivos que ya construimos como arquitectura viva del proyecto.

## 9.4. Revisar `keygo-common`
Si crece rápido, debes imponer reglas o dividirlo.

Señal de alarma:
- clases utilitarias mezcladas,
- enums de negocio fuera de dominio,
- helpers de cualquier cosa.

## 9.5. Sacar lógica de negocio de controllers temprano
Hazlo desde el principio.
El mayor riesgo en proyectos Spring así estructurados es que `controller` termine siendo el caso de uso real.

---

## 10. Estructura objetivo sugerida del repositorio

```text
keygo-server/
├── .github/
├── docs/
├── scripts/
├── keygo-bom/
├── keygo-common/
├── keygo-domain/
├── keygo-app/
├── keygo-api/
├── keygo-infra/
├── keygo-supabase/
├── keygo-run/
├── docker-compose.yml
├── pom.xml
└── README.md
```

### Dentro de `docs/`
Recomendado:

```text
docs/
├── architecture/
│   ├── KEYGO_SERVER_ARCHITECTURE.md
│   ├── KEYGO_SERVER_BACKLOG_V1.md
│   ├── KEYGO_SERVER_DOMAIN_MODEL.md
│   ├── KEYGO_SERVER_API_SURFACE.md
│   └── KEYGO_SERVER_PROJECT_STRUCTURE.md
└── adr/
```

---

## 11. ADRs recomendados a partir de ahora

Te conviene empezar a registrar decisiones arquitectónicas cortas.

ADRs iniciales sugeridos:
- ADR-001: Modular Monolith as initial architecture
- ADR-002: Multi-tenant vía shared schema + tenant_id
- ADR-003: OAuth2/OIDC as auth model
- ADR-004: User and ClientApp modeled separately
- ADR-005: Membership as access relation between user and app
- ADR-006: Subdomain-based tenant resolution

---

## 12. Orden recomendado de refactor sobre tu estructura actual

### Paso 1
Consolidar responsabilidades reales de cada módulo.

### Paso 2
Refactor interno de paquetes en `keygo-api` para dejar de agrupar por nombres genéricos.

### Paso 3
Refactor interno de `keygo-supabase` por feature/subdominio.

### Paso 4
Verificar dependencias Maven para asegurar dirección correcta entre módulos.

### Paso 5
Documentar reglas de ubicación de clases para que el proyecto no se degrade.

---

## 13. Decisión ejecutiva

La estructura actual **va bien encaminada**. No la botaría ni la rehacería completa.

La mejora clave no es cambiar todos los módulos, sino:

- **hacer más estricta la responsabilidad de cada módulo**,
- **organizar internamente por feature**,
- **evitar carpetas genéricas como destino universal**,
- y **proteger la dirección de dependencias**.

En una frase:

> tu base ya sirve para Key-go, pero ahora toca endurecerla para que no se convierta en un Spring monolith clásico disfrazado de clean architecture.

---

## 14. Próximo paso recomendado

Con esta estructura cerrada, el siguiente documento natural es:

# `KEYGO_SERVER_IMPLEMENTATION_PLAN.md`

Debe incluir:
- orden técnico real de construcción,
- qué módulos tocar primero,
- qué clases crear primero,
- dependencias entre entregables,
- y un roadmap sprint a sprint para pasar de la estructura actual al MVP funcional.

