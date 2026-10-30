# Flujo de autenticación JWT

Este documento recoge el flujo propuesto para incorporar autenticación mediante JWT a la aplicación.

```text
POST /api/auth/login
        ↓
AuthenticationManager + BCrypt
        ↓
UserDetailsService carga User por email
        ↓
JwtService firma un token
        ↓
Cliente envía: Authorization: Bearer <token>
        ↓
SecurityFilterChain valida JWT y obtiene rol
        ↓
RoleHierarchy aplica herencias
        ↓
Reglas de endpoint autorizan o deniegan
```

## Orden de implementación

1. Definir el contrato de autenticación.

   - `LoginRequest`: `email` y `password`.
   - `LoginResponse`: token, tipo `Bearer` y tiempo de expiración.
   - Endpoint público: `POST /api/auth/login`.

2. Adaptar el usuario a autenticación.

   Crear un `UserDetailsService` que busque por correo mediante `UserRepository`, rechace usuarios con `enabled = false` y asigne como autoridad `role.name()`.

   El componente de autenticación debe poder comparar la contraseña recibida con el hash almacenado. Ese hash no debe exponerse en DTOs ni respuestas HTTP.

3. Configurar la autenticación por correo y contraseña.

   Utilizar `DaoAuthenticationProvider` con `UserDetailsService` y el `PasswordEncoder` BCrypt existente.

4. Crear el servicio de emisión de tokens.

   Tras autenticar al usuario, generar un JWT con las siguientes claims mínimas:

   ```text
   sub: identificador del usuario
   roles: ADMINISTRATOR | INSPECTOR | MAINTENANCE_TECHNICIAN
   iat: fecha de emisión
   exp: fecha de caducidad
   ```

   El identificador del usuario se usará como `sub`, ya que el correo puede modificarse. La contraseña nunca se incluirá en el token.

5. Definir la clave de firma.

   - Para una aplicación sencilla con un único backend, usar una clave simétrica segura en una variable de entorno.
   - Para varios servicios, usar un par RSA: clave privada para firmar y clave pública para validar.

   Las claves no deben guardarse en `application.properties` ni en el repositorio.

6. Configurar `SecurityConfig`.

   - Configurar sesiones sin estado.
   - Mantener CSRF deshabilitado para autenticación mediante token Bearer.
   - Configurar el recurso OAuth2 para validar JWT.
   - Convertir la claim `roles` en autoridades `ROLE_...`.
   - Mantener la jerarquía de roles ya definida.
   - Permitir únicamente `/actuator/health` y `/api/auth/login` sin autenticación.
   - Sustituir los `permitAll()` temporales de usuarios por reglas de autenticación y autorización.

7. Añadir respuestas de seguridad consistentes.

   Configurar un `AuthenticationEntryPoint` para HTTP 401 y un `AccessDeniedHandler` para HTTP 403. Ambos deben responder usando `ApiResponse`.

8. Aplicar permisos por rol.

   La jerarquía actual es:

   ```text
   ROLE_ADMINISTRATOR
   ├── ROLE_INSPECTOR
   └── ROLE_MAINTENANCE_TECHNICIAN
   ```

   Las reglas concretas de cada endpoint se definirán cuando esté cerrada la matriz de permisos.

9. Definir la invalidación de tokens.

   Un token emitido no se invalida automáticamente al deshabilitar un usuario, cambiar su rol o restablecer su contraseña. Como primera medida, se usarán tokens de vida corta. Si se requiere revocación inmediata, habrá que incorporar una versión de token o una lista de revocación.
