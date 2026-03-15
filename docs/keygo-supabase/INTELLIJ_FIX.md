# IntelliJ IDEA - Fix para Lombok y Annotation Processors / Fix for Lombok and Annotation Processors

## Problema / Problem

Al ejecutar el goal `package` desde IntelliJ IDEA se producen errores de "symbol not found" aunque el IDE reconoce las clases. Cuando se ejecuta `mvnw` desde terminal funciona correctamente.

When running the `package` goal from IntelliJ IDEA, "symbol not found" errors occur even though the IDE recognizes the classes. When running `mvnw` from terminal it works correctly.

## Causa / Cause

IntelliJ IDEA no está procesando correctamente las anotaciones de Lombok. El procesador de anotaciones necesita estar habilitado explícitamente en el IDE.

IntelliJ IDEA is not processing Lombok annotations correctly. The annotation processor needs to be explicitly enabled in the IDE.

## Solución Aplicada / Applied Solution

### 1. Actualización del POM / POM Update

Se agregó la configuración del `maven-compiler-plugin` con los procesadores de anotaciones necesarios:

The `maven-compiler-plugin` configuration was added with the necessary annotation processors:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-configuration-processor</artifactId>
                <version>${spring-boot.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 2. Pasos en IntelliJ IDEA / Steps in IntelliJ IDEA

Sigue estos pasos para asegurar que IntelliJ IDEA funcione correctamente:

Follow these steps to ensure IntelliJ IDEA works correctly:

#### Paso 1: Instalar el Plugin de Lombok / Step 1: Install Lombok Plugin

1. Ve a **File > Settings > Plugins** (en Windows/Linux) o **IntelliJ IDEA > Preferences > Plugins** (en macOS)
2. Busca "Lombok"
3. Instala el plugin "Lombok" si no está instalado
4. Reinicia IntelliJ IDEA si es necesario

---

1. Go to **File > Settings > Plugins** (on Windows/Linux) or **IntelliJ IDEA > Preferences > Plugins** (on macOS)
2. Search for "Lombok"
3. Install the "Lombok" plugin if not installed
4. Restart IntelliJ IDEA if needed

#### Paso 2: Habilitar el Procesamiento de Anotaciones / Step 2: Enable Annotation Processing

1. Ve a **File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors** (Windows/Linux) o **IntelliJ IDEA > Preferences > Build, Execution, Deployment > Compiler > Annotation Processors** (macOS)
2. Marca la casilla **Enable annotation processing**
3. Asegúrate de que **Obtain processors from project classpath** esté seleccionado
4. Haz clic en **Apply** y **OK**

---

1. Go to **File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors** (Windows/Linux) or **IntelliJ IDEA > Preferences > Build, Execution, Deployment > Compiler > Annotation Processors** (macOS)
2. Check the **Enable annotation processing** checkbox
3. Ensure **Obtain processors from project classpath** is selected
4. Click **Apply** and **OK**

#### Paso 3: Recargar el Proyecto Maven / Step 3: Reload Maven Project

1. Abre la vista de Maven (View > Tool Windows > Maven)
2. Haz clic en el icono de **Reload All Maven Projects** (icono de recarga circular)
3. Espera a que termine la sincronización

---

1. Open the Maven view (View > Tool Windows > Maven)
2. Click on the **Reload All Maven Projects** icon (circular reload icon)
3. Wait for the synchronization to complete

#### Paso 4: Reconstruir el Proyecto / Step 4: Rebuild Project

1. Ve a **Build > Rebuild Project**
2. Espera a que termine la compilación

---

1. Go to **Build > Rebuild Project**
2. Wait for the compilation to complete

#### Paso 5: Invalidar Cachés (si es necesario) / Step 5: Invalidate Caches (if needed)

Si después de los pasos anteriores aún hay problemas:

If after the previous steps there are still problems:

1. Ve a **File > Invalidate Caches...**
2. Selecciona **Invalidate and Restart**
3. Confirma la acción

---

1. Go to **File > Invalidate Caches...**
2. Select **Invalidate and Restart**
3. Confirm the action

## Verificación / Verification

### Desde Terminal / From Terminal

```bash
cd keygo-supabase
../mvnw clean compile
```

Debe compilar sin errores.

Should compile without errors.

### Desde IntelliJ IDEA / From IntelliJ IDEA

1. Haz clic derecho en el módulo `keygo-supabase`
2. Selecciona **Maven > Lifecycle > package**
3. Debe ejecutarse sin errores de "symbol not found"

---

1. Right-click on the `keygo-supabase` module
2. Select **Maven > Lifecycle > package**
3. Should run without "symbol not found" errors

## Configuración del Runner / Runner Configuration

Para ejecutar la aplicación desde IntelliJ:

To run the application from IntelliJ:

1. Ve a **Run > Edit Configurations...**
2. Haz clic en el **+** y selecciona **Spring Boot**
3. Configura:
   - **Name**: KeyGo Server
   - **Main class**: `io.cmartinezs.keygo.run.KeyGoRunner` (o la clase principal correspondiente)
   - **Module**: `keygo-run`
   - **Working directory**: `$MODULE_WORKING_DIR$`
4. Haz clic en **Apply** y **OK**
5. Ahora puedes ejecutar con el botón de **Run** (▶️)

---

1. Go to **Run > Edit Configurations...**
2. Click on the **+** and select **Spring Boot**
3. Configure:
   - **Name**: KeyGo Server
   - **Main class**: `io.cmartinezs.keygo.run.KeyGoRunner` (or the corresponding main class)
   - **Module**: `keygo-run`
   - **Working directory**: `$MODULE_WORKING_DIR$`
4. Click **Apply** and **OK**
5. Now you can run with the **Run** button (▶️)

## Notas Adicionales / Additional Notes

### Variables de Lombok / Lombok Variables

Si las propiedades `${lombok.version}` o `${spring-boot.version}` no están definidas en el POM padre, es necesario agregarlas o especificar las versiones directamente.

If the `${lombok.version}` or `${spring-boot.version}` properties are not defined in the parent POM, they need to be added or the versions specified directly.

### Compatibilidad / Compatibility

Esta configuración es compatible con:
- IntelliJ IDEA 2020.3 o superior
- Maven 3.6 o superior
- Java 21
- Spring Boot 4.0.3
- Lombok (gestionado por Spring Boot Parent)

This configuration is compatible with:
- IntelliJ IDEA 2020.3 or higher
- Maven 3.6 or higher
- Java 21
- Spring Boot 4.0.3
- Lombok (managed by Spring Boot Parent)

## Referencias / References

- [Lombok Plugin Documentation](https://plugins.jetbrains.com/plugin/6317-lombok)
- [IntelliJ IDEA Annotation Processors](https://www.jetbrains.com/help/idea/annotation-processors.html)
- [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)

