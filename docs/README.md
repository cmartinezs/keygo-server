# Documentation / Documentación

[English](#english) | [Español](#español)

---

## English

This directory contains technical and detailed documentation for the KeyGo Server project, organized by module.

### Structure

```
docs/
├── keygo-server/         General project docs (architecture, docker, tooling)
│   └── changes/          History of major changes and milestones
├── keygo-run/            Boot, filters and configuration properties
├── keygo-api/            Controllers, response codes and REST API
└── keygo-supabase/       Supabase integration and database migrations
```

### Modules

- **[keygo-server/](keygo-server/)** - Architecture, Docker, Lombok, IntelliJ build tips, test strategy
  - **[keygo-server/changes/](keygo-server/changes/)** - Summaries of major project changes
- **[keygo-run/](keygo-run/)** - Bootstrap properties and security filter documentation
- **[keygo-api/](keygo-api/)** - Response codes guide, service info endpoint, ResponseEntity refactoring
- **[keygo-supabase/](keygo-supabase/)** - Supabase integration, migrations, environment strategy, IntelliJ setup

### How to Use

1. **For Contributors**: Read `keygo-server/ARCHITECTURE.md` to understand the project structure before coding
2. **For Reviewers**: Reference these docs when reviewing PRs
3. **For History**: Check `keygo-server/changes/` folder for context on major decisions

---

## Español

Este directorio contiene documentación técnica y detallada del proyecto KeyGo Server, organizada por módulo.

### Estructura

```
docs/
├── keygo-server/         Documentación general del proyecto (arquitectura, docker, herramientas)
│   └── changes/          Historial de cambios importantes y milestones
├── keygo-run/            Arranque, filtros y propiedades de configuración
├── keygo-api/            Controladores, códigos de respuesta y API REST
└── keygo-supabase/       Integración con Supabase y migraciones de base de datos
```

### Módulos

- **[keygo-server/](keygo-server/)** - Arquitectura, Docker, Lombok, tips de IntelliJ, estrategia de tests
  - **[keygo-server/changes/](keygo-server/changes/)** - Resúmenes de cambios importantes del proyecto
- **[keygo-run/](keygo-run/)** - Bootstrap properties y documentación del filtro de seguridad
- **[keygo-api/](keygo-api/)** - Guía de códigos de respuesta, endpoint service info, refactoring ResponseEntity
- **[keygo-supabase/](keygo-supabase/)** - Integración Supabase, migraciones, estrategia de entorno, configuración IntelliJ

### Cómo Usar

1. **Para Contribuidores**: Lee `keygo-server/ARCHITECTURE.md` para entender la estructura del proyecto antes de codificar
2. **Para Revisores**: Referencia estos documentos al revisar PRs
3. **Para Historial**: Revisa la carpeta `keygo-server/changes/` para contexto sobre decisiones importantes
