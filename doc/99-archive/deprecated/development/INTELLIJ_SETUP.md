# INTELLIJ_SETUP (Archived)

⚠️ **This document is archived — IDE-specific setup is low priority.**

For general development setup, see:
- [`./ENVIRONMENT_SETUP.md`](./ENVIRONMENT_SETUP.md) — Cross-IDE development setup

IDE setup is better documented in:
- IDE built-in help
- Project build instructions
- Code comments in configuration files

Archived in: [`../archive/deprecated/`](../archive/deprecated/)
| Module | `keygo-run` |
| JRE | Java 21 |

## 3. Variables de entorno

La fuente recomendada es `.env` en la raíz del repo.

Flujo:

```bash
./docs/scripts/switch-env.sh local
```

Luego, en IntelliJ:

- usar EnvFile apuntando a `.env`, o
- copiar manualmente las variables necesarias del `.env` activo.

Variables típicas para local:

```text
SPRING_PROFILES_ACTIVE=supabase,local
SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
SUPABASE_USER=postgres
SUPABASE_PASSWORD=postgres
KEYGO_UI_BASE_URL=http://localhost:5173
```

## 4. Verificación

```bash
./mvnw clean package -DskipTests
```

Si compila por terminal pero no por IntelliJ, revisar annotation processing y recarga Maven.

## Referencias

- [`ENVIRONMENT_SETUP.md`](ENVIRONMENT_SETUP.md)
- [`../../README.md`](../../README.md)
