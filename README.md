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
├── keygo-run/        # Execution and startup configuration / Configuración de ejecución y arranque
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


