---
mode: agent
---

# Escribir tests unitarios (KeyGo Server)

Genera tests unitarios enfocados y mantenibles para el código indicado.

## Reglas

- Usar JUnit 5 + AssertJ + Mockito.
- Evitar `@SpringBootTest` si no es necesario — preferir tests puros sin contexto Spring.
- Nombrar la clase de test como `<ClaseAProbar>Test`.
- Nombrar métodos de forma descriptiva: `should_<comportamiento>_when_<condicion>()`.
- Cubrir: caso feliz, edge cases y casos de error/excepción.
- Mockear dependencias externas con `@Mock` / `@InjectMocks`.

## Entrega esperada

- Código de tests listo para pegar en el módulo correspondiente.
- Breve explicación de qué cubre cada test o grupo.
- Comando para correr:

```bash
# todos los módulos
./mvnw test

# módulo específico
./mvnw -pl <modulo> test
```

