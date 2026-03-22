# Configuración de IntelliJ IDEA — KeyGo Server

> **Última actualización:** 2026-03-22  
> Consolida: `INTELLIJ_BUILD_FIX.md`, `LOMBOK_INTEGRATION.md`, `INTELLIJ_FIX.md`,
> `QUICK_FIX.md`, `SOLUTION_SUMMARY.md`, `RUNNER_CONFIGURATION.md`, `INTELLIJ_SETUP.md`

---

## 1. Problema resuelto: Lombok y Annotation Processing

### Síntoma

Al ejecutar el goal `package` desde IntelliJ IDEA aparecen errores de **"symbol not found"**
aunque el IDE reconoce las clases correctamente. Desde terminal (`./mvnw`) compila sin problemas.

Otro síntoma reportado: `java.lang.ExceptionInInitializerError com.sun.tools.javac.code.TypeTag::UNKNOWN`
en versiones antiguas de Lombok con Java 21.

### Causa

IntelliJ IDEA necesita que el procesador de anotaciones de Lombok esté **explícitamente**
configurado en el `maven-compiler-plugin`. Sin esto, no genera los métodos (getters, setters,
builders, etc.) y falla al resolver los símbolos generados.

Adicionalmente, si se especifica una versión de Lombok diferente a la que gestiona el parent
POM de Spring Boot, se producen conflictos con Java 21.

### Solución aplicada en los POMs

Cada módulo que usa Lombok (`keygo-api`, `keygo-common`, `keygo-run`, `keygo-supabase`) tiene
la siguiente configuración en su `pom.xml`, **sin especificar `<version>`** (se deja que
Spring Boot parent gestione la versión, actualmente Lombok `1.18.42` para Spring Boot `4.0.3`):

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <!-- NO especificar <version> aquí — usa la versión gestionada por Spring Boot -->
          </path>
          <path>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

> ⚠️ **Importante:** No incluir `<version>` en `annotationProcessorPaths` evita conflictos
> con la versión gestionada por el parent POM.

Dependencia en cada módulo (sin versión explícita):

```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <scope>provided</scope>
</dependency>
```

**Módulos configurados:**

| Módulo | Usa Lombok |
|---|---|
| `keygo-api` | ✅ |
| `keygo-common` | ✅ |
| `keygo-run` | ✅ |
| `keygo-supabase` | ✅ |
| `keygo-domain` | ❌ (no lo usa) |
| `keygo-app` | ❌ (no lo usa) |
| `keygo-infra` | ❌ (no lo usa) |

---

## 2. Configuración de IntelliJ IDEA

### Paso 1 — Instalar el plugin de Lombok

1. **File → Settings → Plugins** (Windows/Linux)  
   o **IntelliJ IDEA → Preferences → Plugins** (macOS)
2. Pestaña **Marketplace** → Buscar **"Lombok"**
3. Instalar el plugin **"Lombok"** (JetBrains)
4. Reiniciar IntelliJ si se solicita

### Paso 2 — Habilitar Annotation Processing

1. **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
2. Marcar ✅ **Enable annotation processing**
3. Seleccionar ⚫ **Obtain processors from project classpath**
4. Click **Apply → OK**

### Paso 3 — Recargar el proyecto Maven

1. Abrir panel Maven: **View → Tool Windows → Maven**
2. Click en 🔄 **Reload All Maven Projects**
3. Esperar a que termine la sincronización

### Paso 4 — Rebuild

1. **Build → Rebuild Project** (o `Ctrl+Shift+F9`)
2. Verificar que no haya errores de compilación

### Paso 5 — Invalidar cachés (si persisten errores)

1. **File → Invalidate Caches...**
2. Seleccionar **Invalidate and Restart**

---

## 3. Configuración del runner de IntelliJ

### 3.1 Crear configuración Spring Boot

1. **Run → Edit Configurations...** (`Alt+Shift+F10` → `0`)
2. Click **+** → seleccionar **Spring Boot** (o **Application**)
3. Configurar:

| Campo | Valor |
|---|---|
| **Name** | `KeyGo Server` |
| **Main class** | `io.cmartinezs.keygo.run.KeyGoApplication` |
| **Module** | `keygo-run` |
| **JRE** | Java 21 |
| **Working directory** | `$MODULE_WORKING_DIR$` |

4. Click **Apply → OK**

> ⚠️ La clase principal es `KeyGoApplication`. Versiones antiguas de la documentación referenciaban
> `KeyGoRunner` (nombre anterior). Usar siempre `KeyGoApplication`.

### 3.2 Variables de entorno manuales (sin EnvFile)

En el campo **Environment variables** de la configuración del runner:

```
SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
SUPABASE_USER=postgres
SUPABASE_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=supabase,local
KEYGO_ADMIN_KEY=changeMe
```

### 3.3 Configuración con EnvFile plugin (recomendado para Supabase)

El plugin **EnvFile** permite cargar variables desde el archivo `keygo-supabase/.env`.

#### Instalar EnvFile

1. **File → Settings → Plugins → Marketplace**
2. Buscar **"EnvFile"** (by Borys Pierov)
3. Instalar y reiniciar el IDE

#### Configurar en el runner

1. Abrir la configuración del runner (`Run → Edit Configurations...`)
2. Pestaña **EnvFile**
3. Marcar ✅ **Enable EnvFile**
4. Click **+** → navegar a `keygo-supabase/.env`
5. Marcar ✅ **Substitute environment variables**
6. Click **Apply → OK**

#### Configuración XML equivalente (`.idea/runConfigurations/`)

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="KeyGo Server (Local)" type="SpringBootApplicationConfigurationType">
    <option name="ACTIVE_PROFILES" value="supabase,local" />
    <module name="keygo-run" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="io.cmartinezs.keygo.run.KeyGoApplication" />
    <extension name="net.ashald.envfile">
      <option name="IS_ENABLED" value="true" />
      <option name="IS_SUBST" value="true" />
      <ENTRIES>
        <ENTRY IS_ENABLED="true" PARSER="runconfig" IS_EXEC="false"
               PATH="$PROJECT_DIR$/keygo-supabase/.env" />
      </ENTRIES>
    </extension>
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
```

---

## 4. Verificación

```bash
# Desde terminal — verificación rápida
./mvnw clean package -DskipTests

# Resultado esperado:
# [INFO] BUILD SUCCESS
```

Desde IntelliJ:
1. Click derecho en cualquier módulo → **Maven → Lifecycle → package**
2. Debe compilar sin errores de "symbol not found"

---

## 5. Troubleshooting

| Síntoma | Solución |
|---|---|
| "cannot find symbol" en métodos de Lombok | Verificar plugin instalado + annotation processing habilitado + reload Maven |
| Maven compila pero IntelliJ muestra errores | Rebuild Project → si persiste, Invalidate Caches |
| "Module not specified" al ejecutar | Seleccionar módulo `keygo-run` en la configuración del runner |
| "Error: Could not find or load main class" | Verificar que la clase sea `io.cmartinezs.keygo.run.KeyGoApplication` + Rebuild |
| Variables de entorno no cargadas | Verificar que EnvFile esté habilitado y apunte a `keygo-supabase/.env` |
| `TypeTag::UNKNOWN` en IntelliJ | Versión de Lombok incorrecta en POM — eliminar `<version>` del annotationProcessorPath |

---

## Referencias

- [Project Lombok](https://projectlombok.org/)
- [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)
- [IntelliJ Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
- [EnvFile Plugin](https://plugins.jetbrains.com/plugin/7861-envfile)
- Guía de entornos: [`ENVIRONMENT_SETUP.md`](ENVIRONMENT_SETUP.md)

