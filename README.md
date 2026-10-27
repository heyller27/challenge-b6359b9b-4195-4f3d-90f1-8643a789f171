# Fortalecer Habilidades de Framework y Seguridad en Aplicación Java

Como desarrollador avanzado de backend en Java, tu tarea es fortalecer tu habilidad para escribir código legible y reducir la redundancia utilizando el framework más relevante. Además, debes implementar mecanismos de autenticación y comprender el uso de JWT en un proyecto enterprise.

## Informacion General

| Campo | Valor |
|-------|-------|
| **Tema** | Especialista en Framework con Proficiencia en Seguridad y Legibilidad de Código |
| **Nivel** | advanced-l3 |
| **Tipo** | practical |
| **Tiempo estimado** | 5-6 horas |

## Fases del Reto

### Fase 0: Configuración del Proyecto

**Objetivo:** Obtener el proyecto base funcional enviando el Código Base a un asistente de IA, que lo analizará, corregirá errores y generará un ZIP listo para usar.

**Tiempo estimado:** 15-30 minutos

**Instrucciones:**

- Asegúrate de tener instalado para ejecutar el proyecto: Un IDE o editor de código.
- Copia todo el contenido del campo **Código Base** de este reto — incluyendo el texto de instrucciones que aparece al inicio.
- Abre un asistente de IA (Claude en claude.ai, ChatGPT o Gemini — se recomienda Claude), pega el contenido copiado en el chat y envíalo.
- El asistente analizará los archivos, corregirá errores y generará un archivo ZIP descargable. Descárgalo y extráelo en la carpeta donde quieras trabajar.
- Verifica que el proyecto arranca sin errores.

**Entregable:** El proyecto compila/arranca sin errores.

<details>
<summary>Pistas de conocimiento</summary>

- Copia el Código Base completo incluyendo el texto de instrucciones al inicio — esas instrucciones le indican al asistente exactamente qué hacer con los archivos.
- Si el asistente no genera el ZIP automáticamente al terminar el análisis, escríbele: "genera el ZIP ahora".
- Si el proyecto tiene errores al arrancar, comparte el mensaje de error con el mismo asistente para que lo corrija.

</details>

### Fase 1: Exploración y Configuración del Entorno

**Objetivo:** Configurar el entorno de desarrollo y explorar el framework seleccionado para entender sus componentes y capacidades.

**Tiempo estimado:** 1 hora

**Instrucciones:**

- Identifica y configura el framework más relevante para Java en tu entorno de desarrollo.
- Explora la documentación del framework para entender sus componentes principales y cómo pueden ser utilizados para escribir código más legible y reducir redundancia.

**Entregable:** Entorno de desarrollo configurado y resumen de los componentes principales del framework.

<details>
<summary>Pistas de conocimiento</summary>

- Investiga las mejores prácticas para configurar el entorno de desarrollo con el framework seleccionado.
- Considera cómo el framework puede ayudarte a escribir código más legible y reducir redundancia.

</details>

### Fase 2: Implementación de Mecanismos de Autenticación

**Objetivo:** Implementar al menos dos mecanismos de autenticación y comprender el uso de JWT.

**Tiempo estimado:** 2 horas

**Instrucciones:**

- Implementa al menos dos mecanismos de autenticación en tu aplicación.
- Investiga y comprende el uso de JWT para la autenticación y cómo puede ser integrado en tu aplicación.

**Entregable:** Aplicación con dos mecanismos de autenticación implementados y documentación sobre el uso de JWT.

<details>
<summary>Pistas de conocimiento</summary>

- Investiga diferentes tipos de mecanismos de autenticación y selecciona al menos dos para implementar.
- Lee sobre JWT y cómo puede ser utilizado para mejorar la seguridad de tu aplicación.

</details>

### Fase 3: Refactorización y Optimización del Código

**Objetivo:** Refactorizar el código para mejorar la legibilidad y reducir la redundancia utilizando el framework seleccionado.

**Tiempo estimado:** 2 horas

**Instrucciones:**

- Refactoriza el código de tu aplicación para mejorar la legibilidad y reducir la redundancia utilizando el framework seleccionado.
- Identifica y corrige errores comunes en tu implementación.

**Entregable:** Código refactorizado con mejor legibilidad y menor redundancia, y documentación de los cambios realizados.

<details>
<summary>Pistas de conocimiento</summary>

- Identifica patrones de código repetitivo y busca formas de reducir la redundancia utilizando el framework.
- Lee sobre mejores prácticas para escribir código legible y aplica estas prácticas en tu refactorización.

</details>

## Dimensiones Evaluadas

- **queEs**: ¿Qué es un framework y por qué es importante en el desarrollo de aplicaciones?
- **paraQueSirve**: ¿Para qué sirve JWT en la autenticación y cómo se integra en una aplicación Java?
- **comoSeUsa**: ¿Cómo se implementan mecanismos de autenticación en una aplicación Java utilizando un framework?
- **erroresComunes**: ¿Cuáles son los errores comunes al implementar autenticación y cómo se pueden evitar?
- **queDecisionesImplica**: ¿Qué decisiones implica refactorizar el código para mejorar la legibilidad y reducir la redundancia?

## Criterios de Evaluacion

- Configuración correcta del entorno de desarrollo con el framework seleccionado.
- Implementación de al menos dos mecanismos de autenticación.
- Comprensión y uso de JWT en la autenticación.
- Refactorización del código para mejorar la legibilidad y reducir la redundancia.
- Documentación clara y concisa de los cambios realizados.

---

*Reto generado automaticamente por Challenge Generator - Pragma*

## Entregables

| Fase | Documento |
|------|-----------|
| Fase 2 — Mecanismos de autenticación (HTTP Basic + JWT) | [docs/autenticacion.md](docs/autenticacion.md) |
| Fase 3 — Identificación de código a refactorizar | [docs/refactorizacion.md](docs/refactorizacion.md) |

## Cómo ejecutar

Requisitos: Java 17+, Maven y PostgreSQL con el esquema `reto_seguridad` (`CREATE SCHEMA reto_seguridad;`).

1. Definir las variables de entorno (ver `.env.example`): `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.
2. Levantar la aplicación: `mvn spring-boot:run`. Hibernate crea las tablas `users` y `user_roles`.
3. Crear un usuario (contraseña `Admin123*` en BCrypt):

```sql
INSERT INTO reto_seguridad.users (username, password)
VALUES ('admin', '$2a$10$ONkrZZzxMElhZ9Ol6l4L9e65GPtc/cIiXt/Yo2l84IeOZxjcjq58S');

INSERT INTO reto_seguridad.user_roles (user_id, role)
SELECT id, 'ADMIN' FROM reto_seguridad.users WHERE username = 'admin';
```

4. Probar los dos mecanismos con los comandos de [docs/autenticacion.md](docs/autenticacion.md#cómo-probarlo).

| Endpoint | Acceso |
|----------|--------|
| `POST /api/auth/authenticate` | Público — devuelve un JWT |
| `GET /api/profile` | Basic o JWT — indica el mecanismo usado |
| `/api/payments/**` | Basic o JWT |
