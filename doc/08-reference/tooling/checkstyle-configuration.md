# Checkstyle Configuration — KeyGo Server

Esta configuración de Checkstyle está **alineada con google-java-format** para evitar conflictos con el plugin de IntelliJ.

## Características principales

- ✅ **Indentación: 2 espacios** (estándar Google)
- ✅ **Longitud máxima de línea: 120 caracteres**
- ✅ **No falla el build** — solo warnings (`failOnViolation: false`)
- ✅ **Compatible con Lombok** — configuración relajada para builders y fluent APIs

## Ejecución

```bash
# Verificar estilo durante el build
./mvnw verify

# Solo checkstyle (sin tests)
./mvnw checkstyle:check

# Generar reporte HTML
./mvnw checkstyle:checkstyle
# Ver: target/site/checkstyle.html
```

## Integración con IntelliJ

Para que IntelliJ use **google-java-format** consistente con Checkstyle:

1. Instalar plugin: **Settings → Plugins → "google-java-format"**
2. Activar: **Settings → google-java-format → Enable**
3. Formatear código: **Ctrl+Alt+L** (Linux/Windows) o **Cmd+Opt+L** (Mac)

## Configuración de indentación

| Archivo | Indentación | Detalle |
|---|---|---|
| `*.java` | 2 espacios | Alineado con google-java-format |
| `*.xml` | 2 espacios | Maven POM, config |
| `*.yml` | 2 espacios | Spring Boot config |
| `*.json` | 2 espacios | JSON config |
| `*.properties` | 2 espacios | Properties files |

Ver [`.editorconfig`](../../../.editorconfig) para la configuración completa.

## Reglas de indentación (checkstyle.xml)

```xml
<module name="Indentation">
  <property name="basicOffset" value="2"/>
  <property name="braceAdjustment" value="0"/>
  <property name="caseIndent" value="2"/>
  <property name="throwsIndent" value="4"/>
  <property name="lineWrappingIndentation" value="4"/>
  <property name="arrayInitIndent" value="2"/>
  <property name="forceStrictCondition" value="false"/>
</module>
```

## Troubleshooting

### Conflictos de formato

Si ves cambios masivos de indentación después de ejecutar Checkstyle:

1. **Deshacer los cambios:**
   ```bash
   git checkout -- .
   ```

2. **Aplicar google-java-format en IntelliJ:**
   ```
   Ctrl+Alt+L en cada archivo (o seleccionar múltiples archivos → Code → Reformat Code)
   ```

3. **Verificar que `.editorconfig` tenga `indent_size = 2` para Java**

### Desactivar Checkstyle temporalmente

```bash
# Agregar en línea de comando:
./mvnw verify -Dcheckstyle.skip=true
```

## Referencias

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [google-java-format](https://github.com/google/google-java-format)
- [Checkstyle Documentation](https://checkstyle.org/)

