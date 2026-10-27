# Fase 3 — Identificación de código a refactorizar

> **¿Ya identificaste las partes del código que necesitan refactorización?**
> Sí. Este documento lista cada punto detectado, el archivo, el problema, la propuesta usando
> Spring Boot y la prioridad. Es el análisis previo; los cambios se aplican en la Fase 3.

Criterio de prioridad:
**Alta** = riesgo de seguridad o comportamiento incorrecto ·
**Media** = redundancia o legibilidad que dificulta el mantenimiento ·
**Baja** = mejora de diseño sin impacto inmediato.

---

## 1. Redundancia: código de seguridad duplicado (Alta)

El módulo `com.pragma.payment` reimplementa toda la seguridad que ya existe en `com.example`.

| Duplicado | Original vigente | Problema |
|---|---|---|
| `payment/infrastructure/security/JwtTokenUtil.java` | `application/config/JwtTokenUtil.java` | Dos generadores de JWT; el de payment tiene la clave `"secret"` escrita en el código y otra API (`validateToken(token)` vs `validateToken(token, userDetails)`). Un token válido en uno puede no serlo en el otro. |
| `payment/infrastructure/security/SecurityConfig.java` | `application/config/SecurityConfig.java` | Segundo `SecurityFilterChain` con reglas opuestas (`anyRequest().permitAll()`). Hoy no se carga porque ese paquete no se escanea; si se activara, choca por nombre de bean y abre endpoints. |
| `payment/infrastructure/security/JwtAuthenticationFilter.java` | `application/config/JwtRequestFilter.java` | Filtro **incompleto**: valida el token pero nunca autentica al usuario (`// Aquí deberías crear un objeto de autenticación...`). Además captura `Exception` genérica. |

**Propuesta:** eliminar los tres archivos del módulo payment y dejar una única implementación de
seguridad compartida. Si un módulo necesita reglas propias, declararlas en la misma cadena
(`.antMatchers("/api/payments/**").hasRole(...)`) o con un segundo `SecurityFilterChain`
acotado por `requestMatcher` y `@Order`, nunca copiando clases.

## 2. Configuración duplicada (Alta)

- Conviven `application.yml` y `application.properties`. El `.yml` define
  `spring.security.user.password: password` (credencial escrita en el repo, y además ignorada
  porque existe un `UserDetailsService` propio).
- **Propuesta:** un solo archivo de configuración (`application.yml`), perfiles `local`/`test`,
  y todos los secretos por variables de entorno (como ya se hace con `DB_*` y `JWT_SECRET`).

## 3. `PaymentServiceImpl` — errores comunes (Alta)

| Problema | Propuesta |
|---|---|
| Devuelve `null` cuando no encuentra el pago y el controller lo revisa con `if/else` en dos métodos. | Devolver `Optional<Payment>` o lanzar `PaymentNotFoundException`, manejada en un `@RestControllerAdvice` que responda 404 en formato RFC 7807. |
| `ArrayList` compartido en un bean singleton: no es seguro con peticiones concurrentes. | `ConcurrentHashMap<String, Payment>` indexado por id (o un repositorio JPA real). |
| La búsqueda `stream().filter(p -> p.getId().equals(id))` se repite en tres métodos. | Un único método privado `findById`. |
| `createPayment` permite ids duplicados; `deletePayment` responde 204 aunque el pago no exista. | Validar existencia y responder 409 / 404 según el caso. |

## 4. Estructura de paquetes inconsistente (Media)

- Dos raíces (`com.example` y `com.pragma.payment`), lo que obliga a listar paquetes en
  `scanBasePackages` de `Application`.
- En `com.example`, la capa `application` contiene `config` y `controller`, que son
  infraestructura (adaptadores de entrada y configuración del framework).
- **Propuesta:** una sola raíz `com.pragma` organizada en hexagonal:

```
com.pragma
├── Application.java              # en la raíz → escaneo automático, sin listas manuales
├── auth/
│   ├── domain/        (User, puertos)
│   ├── application/   (casos de uso: autenticar, emitir token)
│   └── infrastructure/(controller, JPA, filtro JWT, SecurityConfig)
└── payment/
    ├── domain/
    ├── application/
    └── infrastructure/
```

## 5. Inyección de dependencias por campo (Media)

`@Autowired` sobre campos en `AuthController`, `JwtRequestFilter` y `PaymentController`,
mientras `JpaUserDetailsService` usa constructor.
**Propuesta:** inyección por constructor con campos `final` en todas las clases: dependencias
explícitas, inmutables y fáciles de reemplazar con mocks en pruebas.

## 6. `JwtTokenUtil` (Media)

| Problema | Propuesta |
|---|---|
| `validateToken` parsea y verifica la firma del token **dos veces** (username y expiración por separado). | Parsear una vez y leer todos los claims de ese resultado. |
| Expiración con número mágico `1000 * 60 * 60 * 10`. | Propiedad `jwt.expiration` en un `@ConfigurationProperties(prefix = "jwt")` junto con el secreto. |
| Retorna `Boolean` (wrapper) sin necesidad. | `boolean`. |
| `jjwt` 0.9.1 usa APIs obsoletas (`signWith(alg, String)`, `Jwts.parser()`) y no valida la longitud de la clave. | Migrar a `jjwt` 0.12.x con `Keys.hmacShaKeyFor(...)` (clave ≥ 256 bits). |

## 7. `AuthController` (Media)

- `ResponseEntity<?>` con comodín → `ResponseEntity<AuthResponse>`.
- `throws AuthenticationException` es innecesario (es unchecked).
- `SecurityContextHolder.setAuthentication(...)` en un login *stateless* no aporta nada.
- `AuthRequest`/`AuthResponse` declarados dentro del archivo del controller → paquete `dto`
  con validación (`@NotBlank`) y `@Valid` en el endpoint.

## 8. `SecurityConfig` con API obsoleta (Media)

- `authorizeRequests()`, `antMatchers()` y la cadena `.and()` están deprecados desde Spring
  Security 5.7.
- **Propuesta:** DSL con lambdas (`authorizeHttpRequests(auth -> auth.antMatchers(...)...)`), que
  además facilita la migración a Spring Boot 3 / Security 6.
- Documentar por qué existe `jwtRequestFilterRegistration` (evita que el filtro se ejecute dos
  veces: una como filtro de servlet y otra dentro de Spring Security).

## 9. Modelo `Payment` (Baja)

- `amount` como `String` → `BigDecimal`; `status` como `String` → `enum PaymentStatus`;
  `currency` → `java.util.Currency` o validación ISO 4217.
- Setters públicos que permiten estados inválidos → `record` inmutable o constructor validado.
- Sin validación de entrada en `POST`/`PUT` → Bean Validation (`spring-boot-starter-validation`).

## 10. Pruebas (Baja)

- `ApplicationTests.contextLoads` necesita PostgreSQL levantado → perfil `test` con H2 o Testcontainers.
- `PaymentServiceTest`: nombres `testXxx` poco descriptivos → `shouldReturnEmptyWhenPaymentDoesNotExist`;
  no cubre casos de error (id inexistente, duplicado).
- No hay pruebas de los mecanismos de autenticación → `@WebMvcTest` + `MockMvc` para Basic, JWT
  válido y 401 (sin credenciales, credenciales erradas, token inválido/expirado).

---

## Ya corregido en Fases 0–2

| Problema | Corrección |
|---|---|
| `UserRepository` no se registraba como bean (paquete fuera del escaneo). | `scanBasePackages`, `@EnableJpaRepositories`, `@EntityScan`. |
| `User` era un `record` usado como entidad JPA. | `UserEntity` en infraestructura; `User` queda como modelo de dominio. |
| URL de PostgreSQL con driver H2. | Driver PostgreSQL y configuración por variables de entorno. |
| Faltaban `AuthenticationManager`, `PasswordEncoder` y un `UserDetailsService` real. | Beans en `SecurityConfig` + `JpaUserDetailsService`. |
| Cast de `UserDetails` a `User` en el login (`ClassCastException`). | Uso de `authentication.getName()`. |
| Token inválido producía 500. | `JwtRequestFilter` captura `JwtException` y responde 401. |
| Clave JWT escrita en el código. | `jwt.secret` desde `JWT_SECRET`. |
| Módulo payment no compilaba (faltaba `Payment`). | Clase `Payment` creada; módulo integrado y protegido por la seguridad global. |

---

## Decisiones que implica la refactorización

- **Eliminar vs. unificar:** borrar la seguridad duplicada de payment reduce código, pero exige
  confirmar que ningún cliente depende de su comportamiento (`permitAll`).
- **Reorganizar paquetes** mejora la legibilidad, pero toca casi todos los archivos: conviene
  hacerlo en un commit aislado, sin cambios de lógica, para que la revisión sea simple.
- **Actualizar dependencias** (`jjwt`, Spring Boot 3) puede romper APIs; se hace después de tener
  pruebas que respalden el comportamiento actual.
- **Orden recomendado:** pruebas de los casos actuales → eliminar duplicados → manejo de errores →
  estructura de paquetes → actualización de dependencias.
