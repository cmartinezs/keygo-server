# keygo-server

Servicio de autenticación de usuarios como alternativa open source para empresas que deseen tercerizar la gestión de identidad. Permite a empresas y terceros registrar sus aplicaciones/servicios y administrar sus propios usuarios, contraseñas y accesos de forma centralizada.

**Características principales:**
- 🔐 Gestión centralizada de autenticación para múltiples aplicaciones empresariales
- 🏢 Registro y administración de servicios/apps empresariales
- 👥 Gestión de usuarios por aplicación
- 🔑 Control de contraseñas y accesos a aplicaciones
- 🆓 Alternativa open source a servicios de autenticación propietarios

## Estado del Proyecto

🚧 **En desarrollo inicial** - Proyecto recién iniciado con configuración base.

## Requisitos

- Java 25

> El proyecto incluye Maven Wrapper (`.mvn`), por lo que no es necesario tener Maven instalado.
> 
> **Nota**: Si el comando `./mvnw` no funciona, dale permisos de ejecución:
> ```bash
> chmod +x mvnw
> ```

## Estructura del Proyecto

Proyecto multi-módulo con arquitectura hexagonal:

```
keygo-server/
├── keygo-common/     # Utilidades y componentes compartidos
├── keygo-domain/     # Lógica de negocio y entidades del dominio
├── keygo-app/        # Casos de uso y servicios de aplicación
├── keygo-infra/      # Implementaciones de infraestructura
├── keygo-api/        # Controladores y API REST
├── keygo-run/        # Configuración de ejecución y arranque
├── keygo-bom/        # Bill of Materials - gestión de dependencias
└── pom.xml           # POM padre del proyecto
```

## Build

Compilar el proyecto completo:

```bash
./mvnw clean install
```

Compilar sin ejecutar tests:

```bash
./mvnw clean install -DskipTests
```

## Ejecutar

```bash
java -jar keygo-run/target/keygo-run-*.jar
```

## Tests

Ejecutar todas las pruebas:

```bash
./mvnw test
```

Ejecutar tests de un módulo específico:

```bash
./mvnw test -pl keygo-domain
```

## Contribuir

Lee nuestra [guía de contribución](CONTRIBUTING.md) para conocer el proceso de desarrollo.

1. Crear una rama desde `master`
2. Realizar cambios y commits descriptivos siguiendo [Conventional Commits](https://www.conventionalcommits.org/)
3. Abrir Pull Request con descripción clara

**Documentación adicional:**
- 📋 [CHANGELOG.md](CHANGELOG.md) - Historia de cambios
- 🔒 [SECURITY.md](SECURITY.md) - Política de seguridad
- 🤝 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) - Código de conducta

## Licencia

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

**Autor:** Carlos Martínez ([@cmartinezs](https://github.com/cmartinezs))


