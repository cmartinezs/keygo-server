# Propuesta de planes de billing para KeyGo

> Análisis comparativo con pricing real de IAM/Auth para construir una escalera comercialmente coherente,
> con matemática simple y ejemplos que validan que ningún plan "pierda sentido".

---

## Problema detectado en la escalera actual

Con los números originales, **el salto entre Personal y Team no cuadra**:

| Plan | Tenants | Apps | Identidades | Admins | Precio |
|------|---------|------|-------------|--------|--------|
| Personal | 1 | 3 | 5 | 1 | US$5 |
| Team | 1 | 10 | 10 | 2 | US$49 |

El precio sube casi **10×**, pero las identidades y admins solo se duplican. Comercialmente, ese salto se sentirá caro.

Además, el mercado IAM hoy está muy distorsionado:

- **Clerk**: free hasta 50 k retained users
- **WorkOS AuthKit**: free hasta 1 M users
- **Auth0 B2B**: parte bastante más arriba en self-serve

Por eso no conviene copiar precios; conviene que **la propia escalera sea coherente**.

---

## Recomendación general

Mantener **2 modelos**:

1. **Planes paquete** para la mayoría: Free, Personal, Team, Business.
2. **Plan flexible** para agencias / estudios / freelance multi-cliente → renombrar a **Flex** o **Studio**
   (el término "freelance" se queda corto para el perfil real del usuario).

> **Regla de oro:** el plan Flex siempre debe salir más caro que el paquete equivalente.
> El paquete recompensa compromiso; el Flex recompensa elasticidad.

---

## Propuesta de ladder corregida

> Opción recomendada: mantener precios bajos, pero subir la capacidad en Team y Business.

| Plan | Tenants | Apps | Identidades activas | Admins | Precio |
|------|---------|------|---------------------|--------|--------|
| Free | 1 | 1 | 3 | 1 | US$0 |
| Personal | 1 | 3 | 5 | 1 | US$5 |
| Team | 1 | 10 | 25 | 3 | US$49 |
| Business | 1 | 30 | 100 | 10 | US$149 |
| Enterprise | custom | custom | custom | custom | custom |

**Por qué esta versión sí tiene lógica:**

- **Personal** → entrada ultra agresiva.
- **Team** → sirve de verdad para una pyme o un SaaS pequeño.
- **Business** → adecuado para una empresa real o un SaaS en crecimiento.
- **Enterprise** → entra cuando ya hay volumen o contrato anual.

---

## Tabla de valores unitarios — Plan Flex

> Para quien maneja muchos clientes pequeños o medianos y no quiere comprar contratos separados.

### Tenants (mensual)

| Rango | Precio unitario |
|-------|----------------|
| 1 a 10 tenants | US$8 c/u |
| 11 a 50 tenants | US$6 c/u |
| 51+ tenants | US$4 c/u |

### Apps (mensual)

| Rango | Precio unitario |
|-------|----------------|
| 1 a 20 apps | US$2 c/u |
| 21 a 100 apps | US$1.50 c/u |
| 101+ apps | US$1 c/u |

### Identidades activas (mensual)

| Rango | Precio unitario |
|-------|----------------|
| 1 a 100 | US$1.20 c/u |
| 101 a 500 | US$0.90 c/u |
| 501+ | US$0.60 c/u |

### Admins

- 1 admin incluido por tenant.
- Admin adicional: **US$4 c/u**.

### Por qué esta tabla funciona

- **Personal** sale mucho más conveniente que Flex si eres chico.
- **Team** sale mejor que Flex si eres una pyme o un SaaS pequeño.
- **Business** sale mejor que Flex si tienes un tenant más grande.
- **Flex gana** cuando tienes muchos tenants distintos y tamaños variables.

Ese es exactamente el equilibrio que se busca.

---

## Comparación real: paquetes vs. Plan Flex

### 1) Caso Personal

**Uso:** 1 tenant · 3 apps · 5 identidades · 1 admin

```
Flex:
  tenant     = US$8
  apps       = 3 × $2   = US$6
  identidades= 5 × $1.2 = US$6
  admin extra= 0

Total Flex = US$20
Personal   = US$5   ✅ el paquete conviene muchísimo
```

---

### 2) Caso Team

**Uso:** 1 tenant · 10 apps · 25 identidades · 3 admins

```
Flex:
  tenant      =  US$8
  apps        = 10 × $2     = US$20
  identidades = 25 × $1.2   = US$30
  admins extra=  2 × $4     =  US$8

Total Flex  = US$66
Team        = US$49   ✅ el paquete sigue siendo mejor
```

---

### 3) Caso Business

**Uso:** 1 tenant · 30 apps · 100 identidades · 10 admins

```
Flex:
  tenant      =  US$8
  apps        = (20 × $2) + (10 × $1.5) = US$55
  identidades = 100 × $1.2              = US$120
  admins extra=   9 × $4               =  US$36

Total Flex  = US$219
Business    = US$149   ✅ perfecto
```

---

### 4) Caso agencia / estudio

**Uso:** 12 tenants · 40 apps · 180 identidades · 12 admins (1 por tenant)

```
Flex:
  tenants     = (10 × $8) + (2 × $6)         =  US$92
  apps        = (20 × $2) + (20 × $1.5)      =  US$70
  identidades = (100 × $1.2) + (80 × $0.9)   = US$192
  admins extra= 0  (1 admin incluido × tenant)

Total Flex  = US$354

vs. 12 × Plan Team = 12 × $49 = US$588   ✅ Flex gana aquí, como debe ser
```

---

## Política interna: "que nadie se sienta estafado"

- Cada paquete debe costar entre **25 % y 50 % menos** que su equivalente en Flex.
- El plan más alto nunca debe tener peor costo efectivo que el inferior.
- Si alguien excede un plan, el siguiente debe verse claramente mejor que pagar excedentes raros.

---

## Qué hacer con Enterprise

Enterprise **no debe venderse como "unlimited" de verdad**. Venderlo como:

- Contrato anual.
- Facturación formal.
- Soporte prioritario.
- Límites muy altos o personalizados.
- Descuento adicional sobre Flex / Business.

### Grilla base para cotizar Enterprise

| Recurso | Precio unitario |
|---------|----------------|
| Tenant | US$5 |
| App | US$1.25 |
| Identidad activa | US$0.75 |
| Admin extra | US$3 |

Desde ahí se aplican:

- Descuento por volumen.
- Descuento por anualidad.
- Mínimos de contrato.

> Enterprise siempre debe ganar frente a comprar muchos planes Team o Business.

---

## Renombrado sugerido

| Nombre actual | Nombre propuesto | Motivo |
|---------------|-----------------|--------|
| Freelance | **Flex** o **Studio** | "Freelance" suena a persona sola; este plan sirve también para agencias, software factories, consultoras y estudios multi-cliente |
| Team | Team | ✅ está bien |
| Business | Business | ✅ está bien |
| Enterprise | Enterprise | ✅ está bien |

---

## Propuesta final

### Precios

| Plan | Precio |
|------|--------|
| Free | US$0 |
| Personal | US$5 / mes |
| Team | US$49 / mes |
| Business | US$149 / mes |
| Flex / Studio | pago por uso |
| Enterprise | custom |

### Límites por plan

| Plan | Tenants | Apps | Identidades activas | Admins |
|------|---------|------|---------------------|--------|
| Free | 1 | 1 | 3 | 1 |
| Personal | 1 | 3 | 5 | 1 |
| Team | 1 | 10 | 25 | 3 |
| Business | 1 | 30 | 100 | 10 |

Con esta escalera la lógica queda sólida y conversa bien con cómo el mercado IAM monetiza hoy:
los grandes suelen regalar bastante en el tramo bajo o cobrar fuerte en B2B/Enterprise,
y luego separan add-ons de soporte o administración.
