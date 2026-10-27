# Fase 2 — Mecanismos de autenticación

La aplicación implementa **dos mecanismos de autenticación** sobre la misma fuente de usuarios
(tabla `reto_seguridad.users`, contraseñas con BCrypt):

| Mecanismo | Cómo se envía | Estado en servidor | Uso típico |
|---|---|---|---|
| **HTTP Basic** | `Authorization: Basic base64(usuario:contraseña)` en **cada** petición | Ninguno | Integraciones internas, herramientas, pruebas |
| **JWT (Bearer token)** | Login una vez → `Authorization: Bearer <token>` en las siguientes | Ninguno (stateless) | APIs REST, SPAs, apps móviles, microservicios |

Ambos se resuelven en la cadena de filtros de Spring Security (`SecurityConfig`):

```
Request ──► JwtRequestFilter ──► BasicAuthenticationFilter ──► ... ──► Controller
              │ "Bearer ..."         │ "Basic ..."
              ▼                      ▼
        valida firma/exp.     AuthenticationManager
        carga UserDetails     (DaoAuthenticationProvider + BCrypt)
              └──────────► SecurityContext ◄──────────┘
```

---

## 1. HTTP Basic

- El cliente envía usuario y contraseña codificados en Base64 (**codificación, no cifrado**).
- Spring los valida con `JpaUserDetailsService` + `BCryptPasswordEncoder` en cada request.
- **Ventajas:** simple, estándar (RFC 7617), sin estado.
- **Desventajas:** la contraseña viaja en cada petición (obligatorio HTTPS), no hay expiración
  ni logout real, y cada request ejecuta BCrypt (costoso a propósito).

## 2. JWT — JSON Web Token (RFC 7519)

### ¿Qué es?

Un token autocontenido y **firmado** con tres partes en Base64URL separadas por puntos:

```
xxxxx.yyyyy.zzzzz
header.payload.signature
```

| Parte | Contenido | Ejemplo |
|---|---|---|
| **Header** | Algoritmo y tipo | `{"alg":"HS256","typ":"JWT"}` |
| **Payload** | *Claims*: datos del sujeto | `{"sub":"admin","iat":1730000000,"exp":1730036000}` |
| **Signature** | `HMACSHA256(header + "." + payload, secret)` | garantiza integridad |

> El payload **no está cifrado**: cualquiera puede decodificarlo (p. ej. en jwt.io).
> Nunca poner contraseñas ni datos sensibles en los claims. La firma solo garantiza que
> nadie lo modificó y que lo emitió quien conoce el secreto.

**Claims registrados más comunes:** `sub` (sujeto), `iat` (emitido en), `exp` (expira en),
`iss` (emisor), `aud` (audiencia), `jti` (id único, útil para revocación).

### Flujo en esta aplicación

1. `POST /api/auth/authenticate` con `{username, password}`.
2. `AuthController` delega en el `AuthenticationManager` (BCrypt contra la BD).
3. Si es válido, `JwtTokenUtil.generateToken` firma un JWT con HS256, `sub = username`
   y `exp = 10 horas`, usando la clave de la variable de entorno `JWT_SECRET`.
4. El cliente guarda el token y lo envía como `Authorization: Bearer <token>`.
5. `JwtRequestFilter` verifica firma y expiración, carga el usuario y crea un
   `JwtAuthenticationToken` en el `SecurityContext`. Si el token es inválido o expiró,
   la petición sigue sin autenticar y Spring responde **401**.

### Ventajas

- **Stateless:** el servidor no guarda sesiones → escala horizontalmente sin sesión compartida.
- La contraseña solo viaja una vez (en el login).
- Interoperable entre servicios y lenguajes; puede llevar roles/claims propios.

### Desventajas y riesgos

- **Revocación difícil:** un token robado es válido hasta `exp`. Mitigar con expiración corta
  + *refresh tokens*, o una lista de revocación por `jti`.
- **Almacenamiento en el cliente:** en navegador preferir cookie `HttpOnly; Secure; SameSite`
  sobre `localStorage` (expuesto a XSS).
- **Algoritmos:** rechazar `alg: none`; con HS256 la clave debe tener ≥ 256 bits.
  Para varios servicios que solo validan, preferir RS256/ES256 (clave pública/privada).

### Mejoras recomendadas (siguiente fase)

- Actualizar `jjwt` 0.9.1 → 0.12.x (API moderna, valida longitud de clave).
- Reducir `exp` a 15–30 min e implementar *refresh token*.
- Agregar `iss`/`aud` y roles como claims.
- Respuestas de error en formato RFC 7807.

---

## Cómo probarlo

Prerrequisito: usuario en BD con contraseña en BCrypt (ver sección "Cómo ejecutar" del README).

**HTTP Basic**

```bash
curl -u admin:'Admin123*' http://localhost:8080/api/profile
# {"username":"admin","roles":["ROLE_ADMIN"],"authenticationMechanism":"HTTP_BASIC"}
```

**JWT**

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123*"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/profile
# {"username":"admin","roles":["ROLE_ADMIN"],"authenticationMechanism":"JWT"}
```

**Casos negativos (deben responder 401)**

```bash
curl -i http://localhost:8080/api/profile                                   # sin credenciales
curl -i -u admin:incorrecta http://localhost:8080/api/profile               # Basic inválido
curl -i -H "Authorization: Bearer abc.def.ghi" http://localhost:8080/api/profile  # JWT inválido
```
