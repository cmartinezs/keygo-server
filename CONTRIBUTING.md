# Contributing to KeyGo Server / Contribución a KeyGo Server

[English](#english) | [Español](#español)

---

## English

Thank you for your interest in contributing to KeyGo Server! 🎉

### How to Contribute

#### 1. Fork and Clone

```bash
git clone https://github.com/[your-user]/keygo-server.git
cd keygo-server
```

#### 2. Create a Branch

```bash
git checkout -b feature/feature-name
# or
git checkout -b fix/bug-name
```

#### 3. Make Changes

- Follow project code conventions
- Add tests for new features
- Ensure all tests pass: `./mvnw test`
- Build the project: `./mvnw clean install`

#### 4. Commit

Use descriptive messages following Conventional Commits:

```bash
git commit -m "feat: add OAuth2 authentication"
git commit -m "fix: correct password validation"
git commit -m "docs: update README with examples"
```

Commit types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code refactoring
- `test`: Add or modify tests
- `chore`: Maintenance tasks

#### 5. Push and Pull Request

```bash
git push origin feature/feature-name
```

Then open a Pull Request on GitHub with:
- Clear description of changes
- References to related issues
- Screenshots if applicable

### Code Standards

#### Java
- Use Java 25
- Follow standard Java naming conventions
- Document public classes and methods with JavaDoc
- Keep methods small and cohesive

#### Architecture
- Respect hexagonal architecture
- Maintain layer separation:
  - `domain`: Pure business logic (no external dependencies)
  - `app`: Use cases and application services
  - `infra`: Persistence implementations, external APIs
  - `api`: REST Controllers
  
#### Tests
- Write unit tests for domain logic
- Integration tests for API and persistence
- Maintain code coverage > 80%

### Reporting Bugs

If you find a bug:
1. Check it's not already reported in [Issues](https://github.com/cmartinezs/keygo-server/issues)
2. Create a new issue with:
   - Clear problem description
   - Steps to reproduce
   - Expected vs actual behavior
   - Java version and operating system

### Proposing Features

To propose new features:
1. Open a "Feature Request" issue
2. Describe use case and benefits
3. Wait for feedback before implementing

### Code of Conduct

- Be respectful and constructive
- Accept constructive criticism
- Focus on what's best for the project

### License

By contributing, you agree that your contributions will be licensed under AGPL-3.0, same as the rest of the project.

---

Questions? Open an issue or contact the maintainer.

---

## Español

¡Gracias por tu interés en contribuir a KeyGo Server! 🎉

### Cómo Contribuir

#### 1. Fork y Clone

```bash
git clone https://github.com/[tu-usuario]/keygo-server.git
cd keygo-server
```

#### 2. Crear una rama

```bash
git checkout -b feature/nombre-funcionalidad
# o
git checkout -b fix/nombre-bug
```

#### 3. Realizar cambios

- Sigue las convenciones de código del proyecto
- Añade tests para nuevas funcionalidades
- Asegúrate de que todos los tests pasen: `./mvnw test`
- Compila el proyecto: `./mvnw clean install`

#### 4. Commit

Usa mensajes descriptivos siguiendo Conventional Commits:

```bash
git commit -m "feat: añadir autenticación OAuth2"
git commit -m "fix: corregir validación de contraseñas"
git commit -m "docs: actualizar README con ejemplos"
```

Tipos de commit:
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bugs
- `docs`: Documentación
- `refactor`: Refactorización de código
- `test`: Añadir o modificar tests
- `chore`: Tareas de mantenimiento

#### 5. Push y Pull Request

```bash
git push origin feature/nombre-funcionalidad
```

Luego abre un Pull Request en GitHub con:
- Descripción clara de los cambios
- Referencias a issues relacionados
- Screenshots si aplica

### Estándares de Código

#### Java
- Usar Java 25
- Seguir convenciones de nombres estándar de Java
- Documentar clases y métodos públicos con JavaDoc
- Mantener métodos pequeños y cohesivos

#### Arquitectura
- Respetar la arquitectura hexagonal
- Mantener la separación de capas:
  - `domain`: Lógica de negocio pura (sin dependencias externas)
  - `app`: Casos de uso y servicios de aplicación
  - `infra`: Implementaciones de persistencia, APIs externas
  - `api`: Controladores REST
  
#### Tests
- Escribir tests unitarios para lógica de dominio
- Tests de integración para API y persistencia
- Mantener cobertura de código > 80%

### Reportar Bugs

Si encuentras un bug:
1. Verifica que no esté ya reportado en [Issues](https://github.com/cmartinezs/keygo-server/issues)
2. Crea un nuevo issue con:
   - Descripción clara del problema
   - Pasos para reproducir
   - Comportamiento esperado vs actual
   - Versión de Java y sistema operativo

### Proponer Funcionalidades

Para proponer nuevas funcionalidades:
1. Abre un issue de tipo "Feature Request"
2. Describe el caso de uso y beneficios
3. Espera feedback antes de implementar

### Código de Conducta

- Sé respetuoso y constructivo
- Acepta críticas constructivas
- Enfócate en lo mejor para el proyecto

### Licencia

Al contribuir, aceptas que tus contribuciones se licencien bajo AGPL-3.0, igual que el resto del proyecto.

---

¿Dudas? Abre un issue o contacta al mantenedor.


