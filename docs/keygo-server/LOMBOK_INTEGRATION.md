# Integración de Lombok / Lombok Integration

## Problema Resuelto / Problem Solved

El proyecto experimentaba errores de compilación en IntelliJ IDEA con el error `java.lang.ExceptionInInitializerError com.sun.tools.javac.code.TypeTag::UNKNOWN`, a pesar de que:
- Annotation Processing estaba habilitado en IntelliJ
- El plugin de Lombok estaba instalado en IntelliJ
- Maven compilaba correctamente
- El código fuente estaba correcto

The project was experiencing compilation errors in IntelliJ IDEA with the error `java.lang.ExceptionInInitializerError com.sun.tools.javac.code.TypeTag::UNKNOWN`, even though:
- Annotation Processing was enabled in IntelliJ
- Lombok plugin was installed in IntelliJ
- Maven compiled successfully
- Source code was correct

## Causa / Cause

Había una inconsistencia entre la versión de Lombok especificada en el `maven-compiler-plugin` (1.18.34) y la versión gestionada por Spring Boot parent (1.18.42). Esta discrepancia de versiones, combinada con Java 21, causaba el error `TypeTag::UNKNOWN` en IntelliJ IDEA.

There was an inconsistency between the Lombok version specified in the `maven-compiler-plugin` (1.18.34) and the version managed by Spring Boot parent (1.18.42). This version mismatch, combined with Java 21, caused the `TypeTag::UNKNOWN` error in IntelliJ IDEA.

## Solución / Solution

### 1. Configuración de Maven / Maven Configuration

Se configuró el `maven-compiler-plugin` en cada módulo que utiliza Lombok (`keygo-api`, `keygo-common`, `keygo-run`) **sin especificar una versión hardcodeada**, permitiendo que Spring Boot parent gestione la versión de Lombok:

The `maven-compiler-plugin` was configured in each module that uses Lombok (`keygo-api`, `keygo-common`, `keygo-run`) **without specifying a hardcoded version**, allowing Spring Boot parent to manage the Lombok version:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <!-- NO especificar <version> aquí, usa la versión de Spring Boot -->
                        <!-- DO NOT specify <version> here, use Spring Boot's managed version -->
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Importante / Important:** No incluir la etiqueta `<version>` en el `annotationProcessorPaths` permite que Maven use la versión gestionada por el parent POM de Spring Boot, evitando conflictos de versiones.

Not including the `<version>` tag in `annotationProcessorPaths` allows Maven to use the version managed by Spring Boot's parent POM, avoiding version conflicts.

### 2. Dependencia de Lombok / Lombok Dependency

Cada módulo también debe tener la dependencia de Lombok (sin versión explícita):

Each module must also have the Lombok dependency (without explicit version):

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

### 3. Gestión de Versiones / Version Management

La versión de Lombok es gestionada automáticamente por Spring Boot parent. Para Spring Boot 4.0.1, se usa Lombok 1.18.42.

The Lombok version is automatically managed by Spring Boot parent. For Spring Boot 4.0.1, Lombok 1.18.42 is used.

**No es necesario** declarar la versión en el módulo BOM (`keygo-bom/pom.xml`) ya que Spring Boot ya la gestiona.

**It is not necessary** to declare the version in the BOM module (`keygo-bom/pom.xml`) since Spring Boot already manages it.

## Configuración de IntelliJ IDEA / IntelliJ IDEA Configuration

Para que IntelliJ IDEA funcione correctamente con Lombok:

1. **Instalar el plugin de Lombok:**
   - File → Settings → Plugins
   - Buscar "Lombok"
   - Instalar y reiniciar

2. **Habilitar Annotation Processing:**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Marcar "Enable annotation processing"

3. **Recargar proyecto Maven:**
   - Click derecho en `pom.xml` → Maven → Reload Project
   - O desde la ventana de Maven: Click en el icono de "Reload All Maven Projects"

4. **Invalidar caché (si es necesario):**
   - File → Invalidate Caches → Invalidate and Restart

## Verificación / Verification

Para verificar que todo funciona correctamente:

```bash
# Compilar desde línea de comandos
./mvnw clean install

# Ejecutar la aplicación
./mvnw spring-boot:run -pl keygo-run
```

## Módulos Configurados / Configured Modules

Los siguientes módulos tienen configuración de Lombok:

- ✅ `keygo-common` - Utilidades compartidas
- ✅ `keygo-api` - Controladores REST y DTOs
- ✅ `keygo-run` - Módulo de ejecución

Los siguientes módulos **no necesitan** configuración de Lombok (no lo usan actualmente):

- `keygo-domain` - Entidades de dominio
- `keygo-app` - Casos de uso
- `keygo-infra` - Implementaciones de infraestructura

## Troubleshooting

### Error: "cannot find symbol" en métodos generados por Lombok

**Solución:**
1. Verificar que el plugin de Lombok está instalado en IntelliJ
2. Verificar que Annotation Processing está habilitado
3. Recargar el proyecto Maven
4. Invalidar caché de IntelliJ

### Maven compila pero IntelliJ muestra errores

**Solución:**
1. Reimportar proyecto Maven
2. Build → Rebuild Project
3. Verificar que la versión del SDK es Java 21

### Cambios no se reflejan después de editar anotaciones

**Solución:**
1. Build → Rebuild Project
2. Ejecutar `./mvnw clean compile`

## Referencias / References

- [Project Lombok](https://projectlombok.org/)
- [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)
- [IntelliJ IDEA Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok)

