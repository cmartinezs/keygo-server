# Configuración del Runner en IntelliJ IDEA / IntelliJ IDEA Runner Configuration

## 🏃 Configuración de Spring Boot Runner / Spring Boot Runner Configuration

Para ejecutar el servidor KeyGo desde IntelliJ IDEA, sigue estos pasos:

To run the KeyGo server from IntelliJ IDEA, follow these steps:

### Paso 1: Crear Configuración / Step 1: Create Configuration

1. Ve a **Run > Edit Configurations...** (o **Alt+Shift+F10** → **0** en Windows/Linux)
2. Haz clic en el **+** (Add New Configuration)
3. Selecciona **Application** o **Spring Boot**

### Paso 2: Configurar / Step 2: Configure

#### Si usas Application:
- **Name**: `KeyGo Server`
- **Main class**: `io.cmartinezs.keygo.run.KeyGoRunner`
- **Module**: `keygo-run`
- **JRE**: Java 21 (o la versión de Java que estés usando)
- **Working directory**: `$MODULE_WORKING_DIR$`
- **Environment variables**: (opcional, según tu configuración)

#### Si usas Spring Boot:
- **Name**: `KeyGo Server`
- **Main class**: `io.cmartinezs.keygo.run.KeyGoRunner`
- **Active profiles**: (opcional, por ejemplo: `dev`, `local`)
- **Module**: `keygo-run`
- **JRE**: Java 21
- **Working directory**: `$MODULE_WORKING_DIR$`

### Paso 3: Variables de Entorno (Opcional) / Step 3: Environment Variables (Optional)

Si necesitas configurar Supabase u otras variables:

```
SUPABASE_URL=jdbc:postgresql://localhost:54322/postgres
SUPABASE_USER=postgres
SUPABASE_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=dev
```

### Paso 4: VM Options (Opcional) / Step 4: VM Options (Optional)

Si necesitas configurar opciones de la JVM:

```
-Dspring.profiles.active=dev
-Dserver.port=8080
```

### Paso 5: Guardar y Ejecutar / Step 5: Save and Run

1. Haz clic en **Apply** y **OK**
2. Ahora puedes ejecutar con:
   - Botón **Run** (▶️) para ejecutar normalmente
   - Botón **Debug** (🐛) para ejecutar en modo debug

## 🔧 Troubleshooting

### Problema: "Cannot resolve symbol" en clases de Lombok

**Solución**:
1. Asegúrate de que el plugin de Lombok esté instalado
2. Verifica que el procesamiento de anotaciones esté habilitado
3. Recarga el proyecto Maven
4. Rebuild Project

### Problema: "Module not specified"

**Solución**:
- Asegúrate de seleccionar el módulo `keygo-run` en la configuración

### Problema: "Error: Could not find or load main class"

**Solución**:
1. Verifica que la clase principal sea `io.cmartinezs.keygo.run.KeyGoRunner`
2. Rebuild Project
3. Verifica que el módulo `keygo-run` esté compilado

### Problema: Errores de Spring Boot al iniciar

**Solución**:
1. Verifica que todas las dependencias estén resueltas
2. Recarga el proyecto Maven
3. Verifica las variables de entorno y configuración de base de datos

## 📝 Configuración XML (Avanzado) / XML Configuration (Advanced)

Si prefieres crear la configuración manualmente, puedes agregar este archivo en `.idea/runConfigurations/KeyGo_Server.xml`:

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="KeyGo Server" type="SpringBootApplicationConfigurationType" factoryName="Spring Boot">
    <module name="keygo-run" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="io.cmartinezs.keygo.run.KeyGoRunner" />
    <option name="ACTIVE_PROFILES" value="dev" />
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
```

## 🚀 Ejecución desde Terminal / Terminal Execution

Si prefieres ejecutar desde la terminal integrada de IntelliJ:

```bash
# Compilar el proyecto
./mvnw clean package -DskipTests

# Ejecutar la aplicación
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar

# O usando Maven
./mvnw spring-boot:run -pl keygo-run
```

## 📚 Referencias / References

- [IntelliJ IDEA Run/Debug Configurations](https://www.jetbrains.com/help/idea/run-debug-configuration.html)
- [Spring Boot in IntelliJ IDEA](https://www.jetbrains.com/help/idea/spring-boot.html)

