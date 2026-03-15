# keygo-server

[English](#english) | [Español](#español)

---

## English

User authentication service as an open source alternative for companies wishing to outsource identity management. Allows companies and third parties to register their applications/services and manage their own users, passwords and access centrally.

**Key Features:**
- 🔐 Centralized authentication management for multiple enterprise applications
- 🏢 Registration and administration of enterprise services/apps
- 👥 User management per application
- 🔑 Password and application access control
- 🆓 Open source alternative to proprietary authentication services

### Project Status

🚧 **Initial development** - Project just started with base configuration.

---

## Español

Servicio de autenticación de usuarios como alternativa open source para empresas que deseen tercerizar la gestión de identidad. Permite a empresas y terceros registrar sus aplicaciones/servicios y administrar sus propios usuarios, contraseñas y accesos de forma centralizada.

**Características principales:**
- 🔐 Gestión centralizada de autenticación para múltiples aplicaciones empresariales
- 🏢 Registro y administración de servicios/apps empresariales
- 👥 Gestión de usuarios por aplicación
- 🔑 Control de contraseñas y accesos a aplicaciones
- 🆓 Alternativa open source a servicios de autenticación propietarios

### Estado del Proyecto

🚧 **En desarrollo inicial** - Proyecto recién iniciado con configuración base.

---

## Requirements / Requisitos

**English:**
- Java 25

> The project includes Maven Wrapper (`.mvn`), so Maven installation is not required.
> 
> **Note**: If the `./mvnw` command doesn't work, give it execution permissions:
> ```bash
> chmod +x mvnw
> ```

**Español:**
- Java 25

> El proyecto incluye Maven Wrapper (`.mvn`), por lo que no es necesario tener Maven instalado.
> 
> **Nota**: Si el comando `./mvnw` no funciona, dale permisos de ejecución:
> ```bash
> chmod +x mvnw
> ```

---

## Project Structure / Estructura del Proyecto

**English:** Multi-module project with hexagonal architecture

**Español:** Proyecto multi-módulo con arquitectura hexagonal

```
keygo-server/
├── keygo-common/     # Shared utilities / Utilidades compartidas
├── keygo-domain/     # Business logic and domain entities / Lógica de negocio y entidades del dominio
├── keygo-app/        # Use cases and application services / Casos de uso y servicios de aplicación
├── keygo-infra/      # Infrastructure implementations / Implementaciones de infraestructura
├── keygo-api/        # REST Controllers and API / Controladores y API REST
├── keygo-run/        # Spring Boot runnable (main + wiring + config)
├── keygo-supabase/   # Supabase integration: JPA/Flyway/scripts/compose / Integración Supabase
├── keygo-bom/        # Bill of Materials - dependency management / Gestión de dependencias
└── pom.xml           # Parent POM / POM padre del proyecto
```

---

## Build

**English:** Compile the entire project

**Español:** Compilar el proyecto completo

```bash
./mvnw clean install
```

**English:** Compile without running tests

**Español:** Compilar sin ejecutar tests

```bash
./mvnw clean install -DskipTests
```

---

## Run / Ejecutar

```bash
java -jar keygo-run/target/keygo-run-*.jar
```

### Using Docker / Usando Docker

**English:** Run with Docker Compose (recommended)

**Español:** Ejecutar con Docker Compose (recomendado)

```bash
docker-compose up -d
```

**English:** Or build and run with Docker

**Español:** O construir y ejecutar con Docker

```bash
docker build -t keygo-server:1.0-SNAPSHOT .
docker run -d -p 8080:8080 --name keygo-server keygo-server:1.0-SNAPSHOT
```

**See full Docker documentation / Ver documentación completa de Docker:** [docs/DOCKER.md](docs/DOCKER.md)

---

## API Endpoints

> The service uses `context-path=/keygo-server` by default.
> El servicio usa `context-path=/keygo-server` por defecto.

Base URL: `http://localhost:8080/keygo-server`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/service/info` | Service metadata |
| `GET` | `/api/v1/response-codes` | Available response codes |
| `GET` | `/actuator/health` | Health check (public) |

**Examples / Ejemplos:**

```bash
curl -s http://localhost:8080/keygo-server/api/v1/service/info | jq
curl -s http://localhost:8080/keygo-server/api/v1/response-codes | jq
curl -s http://localhost:8080/keygo-server/actuator/health | jq
```

Endpoints under `/api/` require the bootstrap admin key header:

```bash
curl -s http://localhost:8080/keygo-server/api/v1/response-codes \
  -H "X-KEYGO-ADMIN: $KEYGO_ADMIN_KEY" | jq
```

---

## Environment Variables / Variables de Entorno

### Core / Núcleo

| Variable | Description / Descripción | Default |
|----------|--------------------------|---------|
| `PORT` | Server port / Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profiles / Perfiles activos | `default` |
| `KEYGO_ADMIN_KEY` | Bootstrap admin key header `X-KEYGO-ADMIN` | `changeMe` ⚠️ |

> ⚠️ **`KEYGO_ADMIN_KEY=changeMe` is for local dev only. Always use a strong key in real environments.**

### Supabase / DB (requires profile `supabase`)

| Variable | Description / Descripción |
|----------|--------------------------|
| `SUPABASE_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/keygo` |
| `SUPABASE_USER` | DB username / Usuario |
| `SUPABASE_PASSWORD` | DB password / Contraseña |
| `SUPABASE_PROJECT_ID` | Project ID (optional) |
| `SUPABASE_ANON_KEY` | Anon key (optional) |
| `SUPABASE_SERVICE_KEY` | Service key (optional) |

---

## Quick Start with Local DB / Inicio Rápido con DB Local

**English:** Start PostgreSQL 15 + PgAdmin locally using Docker Compose:

**Español:** Levanta PostgreSQL 15 + PgAdmin localmente con Docker Compose:

```bash
cd keygo-supabase
./scripts/dev-start.sh
```

- PostgreSQL: `localhost:5432` (db `keygo`, user `postgres`, pass `postgres`)
- PgAdmin: `http://localhost:5050` (email `admin@keygo.local`, pass `admin`)

> ⚠️ **Default credentials: for local development only. / Credenciales por defecto: solo para desarrollo local.**

Set the required env vars and run:

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
export KEYGO_ADMIN_KEY="$(openssl rand -base64 32)"

./mvnw spring-boot:run -pl keygo-run
```

---

## Documentation / Documentación

| File / Archivo | Content / Contenido |
|---|---|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Architecture, modules, flows, CI/CD proposal |
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | Compact context for Copilot/Claude agents |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Contribution standards, testing, PRs |
| [`CLAUDE.md`](CLAUDE.md) | Rules for AI coding agents (Claude/Copilot) |
| [`docs/`](docs/) | Historical docs and per-module guides |

---

## Tests / Pruebas

**English:** Run all tests

**Español:** Ejecutar todas las pruebas

```bash
./mvnw test
```

**English:** Run tests for a specific module

**Español:** Ejecutar tests de un módulo específico

```bash
./mvnw test -pl keygo-domain
```

---

## Contributing / Contribuir

**English:** Read our [contribution guide](CONTRIBUTING.md) to learn about the development process.

**Español:** Lee nuestra [guía de contribución](CONTRIBUTING.md) para conocer el proceso de desarrollo.

1. **English:** Create a branch from `master` / **Español:** Crear una rama desde `master`
2. **English:** Make changes and descriptive commits following [Conventional Commits](https://www.conventionalcommits.org/) / **Español:** Realizar cambios y commits descriptivos siguiendo [Conventional Commits](https://www.conventionalcommits.org/)
3. **English:** Open Pull Request with clear description / **Español:** Abrir Pull Request con descripción clara

**Additional documentation / Documentación adicional:**
- 📋 [CHANGELOG.md](CHANGELOG.md) - Change history / Historia de cambios
- 🔒 [SECURITY.md](SECURITY.md) - Security policy / Política de seguridad
- 🤝 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) - Code of conduct / Código de conducta

---

## License / Licencia

**English:**

This project is licensed under **GNU Affero General Public License v3.0 (AGPL-3.0)** with additional commercial terms.

### License Summary:

- ✅ **Allowed:** Clone, modify, distribute and publish modified versions
- ✅ **Open source:** Any modification must maintain the same license
- ✅ **Attribution:** Original authorship must be maintained
- ⚠️ **Commercial use:** Requires revenue-sharing agreement with original author

For more details, see the [LICENSE](LICENSE) file.

### Commercial Use

If you wish to use this project for commercial purposes, please contact the author to negotiate revenue-sharing terms.

**Español:**

Este proyecto está licenciado bajo **GNU Affero General Public License v3.0 (AGPL-3.0)** con términos comerciales adicionales.

### Resumen de la licencia:

- ✅ **Permitido:** Clonar, modificar, distribuir y publicar versiones modificadas
- ✅ **Código abierto:** Cualquier modificación debe mantener la misma licencia
- ✅ **Atribución:** Se debe mantener la autoría original
- ⚠️ **Uso comercial:** Requiere acuerdo de reparto de ganancias con el autor original

Para más detalles, consulta el archivo [LICENSE](LICENSE).

### Uso Comercial

Si deseas usar este proyecto con fines comerciales, por favor contacta al autor para negociar términos de revenue-sharing.

---

**Author / Autor:** Carlos Martínez ([@cmartinezs](https://github.com/cmartinezs))


