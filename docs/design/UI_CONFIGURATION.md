# KeyGo UI Configuration

> Configuración de la UI para generar enlaces directos en emails, notificaciones y otros contextos.

## Overview

La sección `keygo.ui` en el archivo de configuración YAML permite centralizar la configuración de la UI frontend (rutas, URL base, parámetros) para usarla desde el backend al generar enlaces en emails y notificaciones.

**Ventajas:**
- ✅ URL base configurable por ambiente (local, desa, prod)
- ✅ Rutas de la UI documentadas en un solo lugar
- ✅ Fácil de integrar en emails y adaptadores de notificación
- ✅ Evita hardcoding de URLs en código

## Configuration

### application.yml

```yaml
keygo:
  ui:
    base-url: "${KEYGO_UI_BASE_URL:http://localhost:5173}"
    paths:
      reset-password:
        route: "/reset-password"
        query-params:
          - request-id
```

### Environment variables (.env-*)

```bash
# Local development
KEYGO_UI_BASE_URL=http://localhost:5173

# Staging/Desa
KEYGO_UI_BASE_URL=https://desa.yourdomain.com

# Production
KEYGO_UI_BASE_URL=https://yourdomain.com
```

## Usage in Code

### Inyectar `KeyGoUiProperties` en un componente

```java
import io.cmartinezs.keygo.run.config.KeyGoUiProperties;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {
  
  private final KeyGoUiProperties uiProperties;
  
  public PasswordResetEmailService(KeyGoUiProperties uiProperties) {
    this.uiProperties = uiProperties;
  }
  
  public void sendResetPasswordEmail(String requestId) {
    // Obtener la ruta configurada
    KeyGoUiProperties.UiPath resetPath = 
      uiProperties.getPaths().get("reset-password");
    
    if (resetPath != null) {
      // Construir URL con parámetros
      String resetUrl = resetPath.buildUrl(
        uiProperties.getBaseUrl(),
        Map.of("request-id", requestId)
      );
      
      // Usar resetUrl en el template del email
      // Ejemplo: resetUrl = "http://localhost:5173/reset-password?request-id=550e8400-..."
    }
  }
}
```

### En templates Thymeleaf

```html
<!-- email/password-reset.html -->
<div th:if="${resetUrl}">
  <p>Haz clic en el enlace para restablecer tu contraseña:</p>
  <a th:href="${resetUrl}" class="btn-primary">
    Restablecer Contraseña
  </a>
</div>
```

## Adding New Paths

Para agregar una nueva ruta a la UI:

### 1. Actualizar application.yml

```yaml
keygo:
  ui:
    base-url: "${KEYGO_UI_BASE_URL:http://localhost:5173}"
    paths:
      reset-password:
        route: "/reset-password"
        query-params:
          - request-id
      verify-email:                    # ← Nueva ruta
        route: "/verify-email"
        query-params:
          - code
          - user-id
      billing-checkout:                # ← Otra ruta
        route: "/billing/checkout"
        query-params:
          - session-id
```

### 2. Usar en código

```java
KeyGoUiProperties.UiPath verifyEmailPath = 
  uiProperties.getPaths().get("verify-email");

String verifyUrl = verifyEmailPath.buildUrl(
  uiProperties.getBaseUrl(),
  Map.of("code", "123456", "user-id", userId)
);
// Resultado: "http://localhost:5173/verify-email?code=123456&user-id=..."
```

## Path Examples

| Path | Route | Query Params | Use Case |
|---|---|---|---|
| `reset-password` | `/reset-password` | `[request-id]` | Email de recuperación de contraseña |
| `verify-email` | `/verify-email` | `[code]` | Email de verificación de cuenta |
| `billing-checkout` | `/billing/checkout` | `[session-id]` | Email de confirmación de pago |
| `activate-account` | `/activate` | `[token]` | Email de activación de cuenta |

## Testing

Para probar que la configuración se carga correctamente:

```bash
# En un test unitario
@Test
void shouldLoadUiConfiguration() {
  KeyGoUiProperties props = new KeyGoUiProperties();
  props.setBaseUrl("http://localhost:5173");
  
  KeyGoUiProperties.UiPath resetPath = new KeyGoUiProperties.UiPath();
  resetPath.setRoute("/reset-password");
  resetPath.setQueryParams(List.of("request-id"));
  
  props.getPaths().put("reset-password", resetPath);
  
  String url = resetPath.buildUrl(
    props.getBaseUrl(),
    Map.of("request-id", "uuid123")
  );
  
  assertThat(url).isEqualTo("http://localhost:5173/reset-password?request-id=uuid123");
}
```

Ver `KeyGoUiPropertiesTest` para ejemplos completos.

## Related Documentation

- **Email Templates:** [`docs/design/email/EMAIL_TEMPLATES_INDEX.md`](../design/email/EMAIL_TEMPLATES_INDEX.md)
- **Bootstrap Filter:** [`docs/api/BOOTSTRAP_FILTER.md`](../api/BOOTSTRAP_FILTER.md)
- **Configuration:** [`AGENTS.md`](../../AGENTS.md) — Sección "Spring Boot 4.x & Jackson 3"

