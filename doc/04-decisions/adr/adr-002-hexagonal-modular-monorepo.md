# ADR-002: Hexagonal Modular Monorepo

- Fecha: 2026-04-11
- Estado: accepted

## Contexto

KeyGo Server opera como backend IAM con múltiples módulos Maven y responsabilidades separadas entre dominio, aplicación, infraestructura, API y runtime.

## Decisión

Se mantiene una arquitectura hexagonal en monorepo multi-módulo con estas reglas:

- `keygo-domain` contiene dominio puro y no depende de Spring.
- `keygo-app` orquesta casos de uso y puertos.
- `keygo-infra` y `keygo-supabase` implementan adaptadores.
- `keygo-api` expone contratos HTTP y DTOs.
- `keygo-run` resuelve wiring, seguridad y configuración.

## Consecuencias

- El acoplamiento técnico se controla por módulo y dirección de dependencia.
- Las reglas de dominio permanecen testeables sin framework.
- El costo es mayor disciplina documental y de wiring al agregar funcionalidades.

## Alternativas consideradas

- Monolito Spring sin separación por puertos.
- Microservicios tempranos por capability.
