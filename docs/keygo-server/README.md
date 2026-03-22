# 📁 KeyGo Server — Documentación Técnica

> Documentación de referencia técnica del módulo ejecutable y arquitectura de KeyGo Server.

---

## 📚 Índice de documentos

### 🏗️ Arquitectura y diseño

| Documento | Propósito | Audiencia |
|---|---|---|
| **[ARCHITECTURE.md](./ARCHITECTURE.md)** | Estructura modular, decisiones de diseño, flujos de capas | Arquitectos, Devs Senior |
| **[CODE_STYLE.md](./CODE_STYLE.md)** | Convenciones de nombres, estilo de código, orden de imports | Todos |
| **[TESTING_GUIDE.md](./TESTING_GUIDE.md)** | Estrategia de tests, ejemplos JUnit 5, mocking, integration | QA, Devs |

### 📊 Modelo de datos

| Documento | Propósito | Audiencia |
|---|---|---|
| **[DATA_DICTIONARY.md](./DATA_DICTIONARY.md)** | 🆕 **Índice centralizado** — guía de acceso rápido a diccionario y E/R | Todos |
| **[DATA_MODEL.md](./DATA_MODEL.md)** | 🆕 **Diccionario de datos** — tablas, campos, constraints, guías SQL | Devs, QA |
| **[ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md)** | 🆕 **Relaciones y flujos** — contextos, OAuth2 flow, state machines, índices | Devs, Arquitectos |

### 🚀 Infraestructura y configuración

| Documento | Propósito | Audiencia |
|---|---|---|
| **[DOCKER.md](./DOCKER.md)** | Build Docker, compose, registro, deployment | DevOps, Devs |
| **[INTELLIJ_BUILD_FIX.md](./INTELLIJ_BUILD_FIX.md)** | Soluciones a problemas comunes IntelliJ + Maven | Devs |
| **[INTELLIJ_SETUP.md](./INTELLIJ_SETUP.md)** | Configuración inicial de IntelliJ para el proyecto | Devs |

### 🔧 Integraciones y utilidades

| Documento | Propósito | Audiencia |
|---|---|---|
| **[LOMBOK_INTEGRATION.md](./LOMBOK_INTEGRATION.md)** | Lombok setup, @Getter/@Setter/@Builder, gotchas | Devs |
| **[TEST_DEPENDENCIES_STRATEGY.md](./TEST_DEPENDENCIES_STRATEGY.md)** | Strategy de mocking, test scope, declarative assertions | Devs |

---

## 🎯 Cómo navegar este contenido

### Si necesito entender...

#### → La estructura general del sistema
1. Leer: **[ARCHITECTURE.md](./ARCHITECTURE.md)**
2. Luego: **[DATA_MODEL.md](./DATA_MODEL.md)** § Modelo E/R

#### → Cómo está organizado el modelo de datos
1. Comienza en: **[DATA_DICTIONARY.md](./DATA_DICTIONARY.md)** (índice rápido)
2. Profundiza en: **[DATA_MODEL.md](./DATA_MODEL.md)** (diccionario completo)
3. Entiende flujos en: **[ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md)** (relaciones y negocio)

#### → Cómo fluyen los datos en autenticación
1. Lee: **[ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md)** § Flujo OAuth2 Authorization Code
2. Consulta: **[DATA_MODEL.md](./DATA_MODEL.md)** § Tablas: authorization_codes, refresh_tokens, signing_keys
3. Valida: **[ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md)** § Matriz de decisión de acceso

#### → Cómo escribir tests
1. Guía: **[TESTING_GUIDE.md](./TESTING_GUIDE.md)**
2. Strategy: **[TEST_DEPENDENCIES_STRATEGY.md](./TEST_DEPENDENCIES_STRATEGY.md)**

#### → Cómo seguir el style guide
1. Lee: **[CODE_STYLE.md](./CODE_STYLE.md)**

#### → Cómo resolver problemas de build
1. Soluciones: **[INTELLIJ_BUILD_FIX.md](./INTELLIJ_BUILD_FIX.md)** o **[INTELLIJ_SETUP.md](./INTELLIJ_SETUP.md)**

#### → Cómo hacer Dockerfile o deployar
1. Guía: **[DOCKER.md](./DOCKER.md)**

---

## 📋 Documentos relacionados en otras carpetas

| Ubicación | Documento | Para qué |
|---|---|---|
| `docs/arch/` | **keygo_server_domain_model.md** | Modelo conceptual de dominio (bounded contexts) |
| `docs/arch/` | **keygo_server_implementation_plan.md** | Roadmap técnico de implementación (fases) |
| `docs/arch/` | **keygo_server_architecture.md** | Decisiones de arquitectura hexagonal |
| `docs/keygo-supabase/` | **INTEGRATION.md** | Cómo integrar Supabase/JPA con Spring |
| `docs/keygo-supabase/` | **MIGRATIONS.md** | Flyway migration strategy |
| `docs/keygo-run/` | **BOOTSTRAP_PROPERTIES.md** | Configuración de bootstrap security filter |
| `docs/keygo-api/` | **RESPONSE_CODES_GUIDE.md** | Códigos de respuesta HTTP |
| Raíz: `.github/` | **copilot-instructions.md** | Instrucciones para Copilot (flujo de trabajo) |
| Raíz: | **AGENTS.md** | Quick-start modules, commands, patterns |
| Raíz: | **CLAUDE.md** | Reglas para agentes (flujo obligatorio) |
| Raíz: | **AI_CONTEXT.md** | Contexto general AI + lecciones aprendidas |
| Raíz: | **ROADMAP.md** | Propuestas técnicas y funcionales activas |

---

## 🎓 Ejemplos de consulta rápida

### Pregunta: ¿Cuál es la estructura de la tabla `client_apps`?
**Respuesta:** [DATA_MODEL.md](./DATA_MODEL.md) § Tabla: client_apps

### Pregunta: ¿Cuál es el flujo completo de OAuth2 Authorization Code?
**Respuesta:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § Flujos de autenticación

### Pregunta: ¿Cómo valido si un usuario puede acceder a una app?
**Respuesta:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § Matriz de decisión para acceso a app

### Pregunta: ¿Qué SQL usar para traer roles de un usuario?
**Respuesta:** [DATA_MODEL.md](./DATA_MODEL.md) § Guía 4: Obtener roles asignados

### Pregunta: ¿Cuál es la convención de nombres de clases?
**Respuesta:** [CODE_STYLE.md](./CODE_STYLE.md) § Convenciones de nombres

### Pregunta: ¿Cómo mockear un repository en tests?
**Respuesta:** [TESTING_GUIDE.md](./TESTING_GUIDE.md) + [TEST_DEPENDENCIES_STRATEGY.md](./TEST_DEPENDENCIES_STRATEGY.md)

### Pregunta: ¿Qué índices existen en la DB?
**Respuesta:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § Índices recomendados (con SQL)

---

## 🔗 Contenido destacado (nuevos documentos — 2026-03-22)

### 📊 [DATA_MODEL.md](./DATA_MODEL.md) — Diccionario y E/R completo
- Tabla con todas las entidades + campos + tipos + constraints
- Diagrama E/R en Mermaid mostrando todas las relaciones
- Jerarquía de cascade (qué se elimina cuando)
- 8 guías de consulta SQL de referencia
- Tabla de enumeraciones (ENUM) con valores permitidos
- Tabla de constraints únicos (PK, UK)

### 🔗 [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) — Flujos y contextos
- 5 diagramas de contextos de negocio (Tenant Mgmt, Client Apps, etc.)
- OAuth2 Authorization Code Flow (sequence diagram)
- Verificación de memberships en login (flowchart)
- Refresh Token Flow (sequence)
- Token Revocation logic
- Ciclo de vida de Membership (state machine)
- Asignación de roles a usuarios
- Matriz de decisión para acceso a app
- Flujo de validación en endpoint protegido
- Diagrama de capas lógicas de validación
- SQL para crear índices recomendados

### 📌 [DATA_DICTIONARY.md](./DATA_DICTIONARY.md) — Índice centralizado
- Mapa rápido de acceso por rol (Dev, Arquitecto, QA)
- Vista de diagramas a nivel de detalle (30k pies, 10k pies, 3k pies)
- Ejemplos de uso (3 casos reales)
- Convenciones de nomenclatura
- Checklist de validación antes de cambios
- Enlaces a documentos relacionados

---

## ✅ Checklist para nuevas features

Antes de tocar código, consulta:

- [ ] **Arquitectura**: ¿Cuál es la estructura modular? → **[ARCHITECTURE.md](./ARCHITECTURE.md)**
- [ ] **Modelo de datos**: ¿Qué tablas/campos afecta? → **[DATA_MODEL.md](./DATA_MODEL.md)**
- [ ] **Flujos**: ¿Cuál es el flujo de negocio? → **[ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md)**
- [ ] **Style**: ¿Sigo las convenciones de nombre? → **[CODE_STYLE.md](./CODE_STYLE.md)**
- [ ] **Tests**: ¿Cómo testeo? → **[TESTING_GUIDE.md](./TESTING_GUIDE.md)**
- [ ] **Response codes**: ¿Qué code usar? → **docs/keygo-api/RESPONSE_CODES_GUIDE.md**
- [ ] **Domain**: ¿Cuál es el bounded context? → **docs/arch/keygo_server_domain_model.md**

---

## 🎯 Sugerencias de lectura según tu rol

### 👨‍💻 Desarrollador Frontend / Full-stack
- **Prioridad 1:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) — entiende los flujos OAuth2
- **Prioridad 2:** [DATA_MODEL.md](./DATA_MODEL.md) § Guías de consulta — qué endpoints llamar
- **Prioridad 3:** **docs/keygo-api/RESPONSE_CODES_GUIDE.md** — mapea errores

### 👨‍💻 Desarrollador Backend
- **Prioridad 1:** [DATA_DICTIONARY.md](./DATA_DICTIONARY.md) — checklist antes de empezar
- **Prioridad 2:** [ARCHITECTURE.md](./ARCHITECTURE.md) — estructura modular
- **Prioridad 3:** [DATA_MODEL.md](./DATA_MODEL.md) — diccionario + SQL
- **Prioridad 4:** [CODE_STYLE.md](./CODE_STYLE.md) — convenciones
- **Prioridad 5:** [TESTING_GUIDE.md](./TESTING_GUIDE.md) — cómo testear

### 🏗️ Arquitecto / Tech Lead
- **Prioridad 1:** [ARCHITECTURE.md](./ARCHITECTURE.md) — decisiones de diseño
- **Prioridad 2:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § "Relaciones por contexto" — cómo está modelado el dominio
- **Prioridad 3:** **docs/arch/keygo_server_domain_model.md** — bounded contexts
- **Prioridad 4:** [DATA_MODEL.md](./DATA_MODEL.md) § "Relaciones de dependencia" — cascade rules

### 🧪 QA / Tester
- **Prioridad 1:** [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § "Ciclo de vida", "Matriz de decisión" — casos de prueba
- **Prioridad 2:** [DATA_MODEL.md](./DATA_MODEL.md) § "Enumeraciones" — valores válidos
- **Prioridad 3:** [TESTING_GUIDE.md](./TESTING_GUIDE.md) — cómo escribir tests

### 🚀 DevOps / SRE
- **Prioridad 1:** [DOCKER.md](./DOCKER.md)
- **Prioridad 2:** **docs/keygo-supabase/INTEGRATION.md** — DB setup
- **Prioridad 3:** [ARCHITECTURE.md](./ARCHITECTURE.md) § módulos

---

## 📞 Preguntas frecuentes

**P: ¿Dónde está el diagrama E/R?**  
R: [DATA_MODEL.md](./DATA_MODEL.md) § "Modelo E/R (Diagrama Mermaid)"

**P: ¿Cómo sé si mi cambio va a romper algo?**  
R: Consulta [DATA_MODEL.md](./DATA_MODEL.md) § "Relaciones de dependencia" para ver cascades.

**P: ¿Cuál es la query SQL para traer datos de X?**  
R: [DATA_MODEL.md](./DATA_MODEL.md) § "Guías de consulta común"

**P: ¿Qué states puede tener una membership?**  
R: [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § "Ciclo de vida de memberships"

**P: ¿Cómo fluyen los datos en OAuth2?**  
R: [ENTITY_RELATIONSHIPS.md](./ENTITY_RELATIONSHIPS.md) § "Flujos de autenticación"

**P: ¿Qué enums existen y qué valores tienen?**  
R: [DATA_MODEL.md](./DATA_MODEL.md) § "Notas sobre enumeraciones"

---

## 📈 Changelog — Documentación de modelo

| Fecha | Evento | Referencia |
|---|---|---|
| 2026-03-22 | Creados 3 documentos de modelo de datos (DATA_MODEL, ENTITY_RELATIONSHIPS, DATA_DICTIONARY) | Este changelog |
| 2026-03-21 | Fase 4 completada (Memberships y roles) | AGENTS.md § "Registro de cambios" |

---

**Última actualización:** 2026-03-22 | **Responsable:** AI Agent | **Estado:** ✅ Completo

