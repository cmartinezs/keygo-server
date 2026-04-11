# Firma de tokens y JWKS — keygo-infra

> **Última actualización:** 2026-03-22  
> Módulo: `keygo-infra` — implementaciones: `RsaJwtTokenSigner`, `JwkSetBuilder`, `StandardTokenClaimsFactory`, `PkceVerifier`

---

## 1. Resumen del módulo

`keygo-infra` contiene las implementaciones de infraestructura criptográfica transversal que no dependen de ningún proveedor específico. Depende de `keygo-app` (implementa sus puertos) y de la librería **Nimbus JOSE+JWT**.

```
keygo-app (puertos)
  TokenSignerPort         ← implementado por RsaJwtTokenSigner
  JwksBuilderPort         ← implementado por JwkSetBuilder
  TokenClaimsFactoryPort  ← implementado por StandardTokenClaimsFactory

keygo-infra (implementaciones)
  auth/jwt/RsaJwtTokenSigner
  auth/jwt/StandardTokenClaimsFactory
  auth/jwks/JwkSetBuilder
  auth/security/PkceVerifier   ← @UtilityClass (sin puerto, uso directo)
```

Wiring en `keygo-run`: los beans se declaran en `ApplicationConfig` vía `@Bean`.

---

## 2. RsaJwtTokenSigner

**Clase:** `io.cmartinezs.keygo.infra.auth.jwt.RsaJwtTokenSigner`  
**Puerto:** `TokenSignerPort`  
**Dependencia:** `com.nimbusds:nimbus-jose-jwt`

### Responsabilidad

Firma tokens JWT con la clave privada RSA almacenada en `SigningKey.privateMaterial` (formato PEM PKCS#8). El header JWT incluye el `kid` para que los consumidores identifiquen la clave pública en el JWKS endpoint.

### Algoritmos soportados

| `SigningKeyAlgorithm` | JWS Algorithm |
|---|---|
| `RS256` (default) | `RS256` |
| `RS384` | `RS384` |
| `RS512` | `RS512` |

### Flujo interno

```mermaid
flowchart LR
    A["signJwt(claims, signingKey)"]
    B["loadPrivateKey(PEM)\nPKCS8EncodedKeySpec"]
    C["RSASSASigner"]
    D["JWSHeader(alg, kid)"]
    E["JWTClaimsSet.Builder\n(mapea iss/sub/aud/jti/iat/exp)"]
    F["SignedJWT.sign()"]
    G["serialize() → token string"]

    A --> B --> C
    A --> D
    A --> E
    C --> F
    D --> F
    E --> F
    F --> G
```

### Claims mapeados

| Claim key | Método JWTClaimsSet | Tipo Java |
|---|---|---|
| `iss` | `issuer(String)` | String |
| `sub` | `subject(String)` | String |
| `aud` | `audience(String)` | String |
| `jti` | `jwtID(String)` | String |
| `iat` | `issueTime(Date)` | Long (epoch seg) × 1000 |
| `exp` | `expirationTime(Date)` | Long (epoch seg) × 1000 |
| cualquier otro | `claim(key, value)` | Object |

### Formato de clave privada requerido

La clave debe estar en formato **PEM PKCS#8** (Base64 entre `-----BEGIN PRIVATE KEY-----`):

```
-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgk...
-----END PRIVATE KEY-----
```

Los headers `BEGIN/END` y espacios en blanco se eliminan antes del decode Base64.

---

## 3. JwkSetBuilder

**Clase:** `io.cmartinezs.keygo.infra.auth.jwks.JwkSetBuilder`  
**Puerto:** `JwksBuilderPort`

### Responsabilidad

Convierte una lista de `SigningKey` del dominio al formato **JWK Set** (RFC 7517). Solo expone la clave pública de cada signing key. Las claves con material público inválido o ausente se omiten silenciosamente sin fallar todo el set.

### Salida

```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "kid-activo-001",
      "use": "sig",
      "alg": "RS256",
      "n": "<base64url modulus>",
      "e": "AQAB"
    }
  ]
}
```

### Comportamiento frente a claves inválidas

- `publicMaterial == null` o blank → se omite sin excepción.
- PEM malformado → se omite con `catch (Exception ignored)`.
- Esto asegura que una clave retirada o revocada con material corrupto no rompa el endpoint JWKS.

### Formato de clave pública requerido

PEM **X.509 / SPKI** (Base64 entre `-----BEGIN PUBLIC KEY-----`):

```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgk...
-----END PUBLIC KEY-----
```

---

## 4. StandardTokenClaimsFactory

**Clase:** `io.cmartinezs.keygo.infra.auth.jwt.StandardTokenClaimsFactory`  
**Puerto:** `TokenClaimsFactoryPort`

### Responsabilidad

Construye los `Map<String, Object>` de claims para:

| Token | Spec de referencia | Claims incluidos |
|---|---|---|
| `access_token` | RFC 9068 | `iss`, `sub`, `aud`, `scope`, `jti`, `iat`, `exp` |
| `id_token` | OIDC Core 1.0 | `iss`, `sub`, `aud`, `jti`, `iat`, `exp`, `at_hash`, `nonce`*, `email`*, `name`* |

`*` → solo si no son null/blank.

### Cálculo de `at_hash` (OIDC Core §3.3.2.11)

```
at_hash = base64url_nopadding( SHA-256(access_token)[0..15] )
```

Solo los primeros **16 bytes** del hash SHA-256, codificados en base64url sin padding.

---

## 5. PkceVerifier

**Clase:** `io.cmartinezs.keygo.infra.auth.security.PkceVerifier`  
**Tipo:** `@UtilityClass` (Lombok) — métodos estáticos, no es un bean Spring

### Responsabilidad

Valida que el `code_verifier` enviado por el cliente corresponda al `code_challenge` almacenado al inicio del flujo OAuth2 Authorization Code + PKCE.

### Métodos soportados

| Método | Verificación |
|---|---|
| `plain` | `verifier.equals(challenge)` |
| `S256` | `base64url(SHA-256(verifier)).equals(challenge)` |
| otro | lanza `IllegalArgumentException("Unknown PKCE method: ...")` |

### Uso

```java
boolean valid = PkceVerifier.verify("S256", codeVerifier, storedChallenge);
if (!valid) {
    throw new InvalidPkceVerificationException();
}
```

### Generación del challenge (cliente)

```java
// Paso 1: generar code_verifier (43-128 chars, ASCII sin reservados)
String verifier = generateSecureRandom();

// Paso 2: calcular code_challenge para método S256
String challenge = PkceVerifier.hashAndEncode(verifier);

// Paso 3: enviar en /oauth2/authorize
// code_challenge=<challenge>&code_challenge_method=S256
```

---

## 6. Wiring en ApplicationConfig

```java
// keygo-run/config/ApplicationConfig.java
@Bean
public TokenSignerPort tokenSignerPort() {
    return new RsaJwtTokenSigner();
}

@Bean
public JwksBuilderPort jwksBuilderPort() {
    return new JwkSetBuilder();
}

@Bean
public TokenClaimsFactoryPort tokenClaimsFactoryPort() {
    return new StandardTokenClaimsFactory();
}
// PkceVerifier es @UtilityClass — no requiere bean
```

---

## 7. Dependencias Maven (keygo-infra/pom.xml)

```xml
<dependency>
    <groupId>io.cmartinezs.keygo</groupId>
    <artifactId>keygo-app</artifactId>
</dependency>
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## 8. Consideraciones de seguridad

| Regla | Detalle |
|---|---|
| Clave privada nunca en logs | `privateMaterial` no debe aparecer en ningún log o stacktrace |
| Clave privada nunca en JWKS | `JwkSetBuilder` solo expone `publicMaterial` |
| Clave privada cifrada en DB | `signing_keys.private_material` debe cifrarse en reposo |
| Al menos una clave ACTIVE | El sistema no puede emitir tokens si no hay `SigningKey` con status `ACTIVE` |
| Claves RETIRED siguen en JWKS | Permiten validar tokens existentes hasta su expiración |
| Claves REVOKED no van a JWKS | Se excluyen para forzar invalidación |

---

## 9. Ciclo de vida de una signing key

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Crear clave (generate RSA keypair)
    ACTIVE --> RETIRED : Rotación planificada (nueva clave ACTIVE)
    ACTIVE --> REVOKED : Compromiso o incidente
    RETIRED --> REVOKED : Eliminar definitivamente
    RETIRED --> [*] : Todos los tokens firmados expirados
```

---

## 10. Tests

| Clase | Módulo | Tests |
|---|---|---|
| `RsaJwtTokenSignerTest` | `keygo-infra` | ✅ |
| `StandardTokenClaimsFactoryTest` | `keygo-infra` | ✅ |

```bash
./mvnw -pl keygo-infra test
```

---

## Referencias

- [RFC 7517 — JSON Web Key (JWK)](https://www.rfc-editor.org/rfc/rfc7517)
- [RFC 9068 — JWT Profile for OAuth2 Access Tokens](https://www.rfc-editor.org/rfc/rfc9068)
- [OIDC Core 1.0 §3.3.2.11 — at_hash](https://openid.net/specs/openid-connect-core-1_0.html#HybridIDToken2)
- [RFC 7636 — PKCE](https://www.rfc-editor.org/rfc/rfc7636)
- [Nimbus JOSE+JWT](https://connect2id.com/products/nimbus-jose-jwt)
- [`../02-functional/authentication-flow.md`](../02-functional/authentication-flow.md) — Flujo OAuth2 completo
- [`../08-reference/data/data-model.md`](../08-reference/data/data-model.md) — Tabla `signing_keys`

