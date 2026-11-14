package eus.birtlh.g3.config;

import eus.birtlh.g3.auth.config.JwtRolesConverter;
import eus.birtlh.g3.auth.security.RestAccessDeniedHandler;
import eus.birtlh.g3.auth.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de autenticación JWT y autorización HTTP de la aplicación.
 *
 * <p>La API no mantiene sesión en servidor y obtiene las autoridades desde el claim {@code role}
 * de los JWT validados. Las rutas se autorizan por rol de acuerdo con el recurso y la operación;
 * cualquier ruta no declarada se deniega explícitamente.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Construye la cadena de filtros de seguridad sin estado de la aplicación.
     *
     * @param http configurador de seguridad HTTP proporcionado por Spring Security
     * @param jwtRolesConverter conversor del claim {@code role} a autoridad de Spring Security
     * @param authenticationEntryPoint generador de respuestas JSON para peticiones no autenticadas
     * @param accessDeniedHandler generador de respuestas JSON para accesos no autorizados
     * @return cadena de filtros con validación JWT y reglas de autorización configuradas
     * @throws Exception si Spring Security no puede construir la cadena de filtros
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRolesConverter jwtRolesConverter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/g3-console.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/machines", "/api/machines/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/machines").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/machines/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers("/api/preventive-plans", "/api/preventive-plans/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers("/api/preventive-plan-assignments", "/api/preventive-plan-assignments/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/preventives/statuses").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_INSPECTOR")
                        .requestMatchers(HttpMethod.GET, "/api/preventives", "/api/preventives/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/preventives/*/assignment").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/preventives/*/start", "/api/preventives/*/work-performed", "/api/preventives/*/completion").hasAuthority("ROLE_INSPECTOR")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter(jwtRolesConverter)
                        ))
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .build();
    }

    /**
     * Crea el conversor que adapta un JWT validado a la autenticación de Spring Security.
     *
     * <p>Delega la conversión del claim {@code role} en {@link JwtRolesConverter} para que las
     * reglas de autorización puedan usar la autoridad {@code ROLE_*} resultante.</p>
     *
     * @param jwtRolesConverter conversor del rol incluido en el token
     * @return conversor de JWT a autenticación con sus autoridades
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter(
            JwtRolesConverter jwtRolesConverter
    ) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtRolesConverter);
        return converter;
    }

    /**
     * Expone el gestor de autenticación configurado por Spring Security.
     *
     * @param configuration configuración de autenticación proporcionada por Spring Security
     * @return gestor encargado de autenticar las credenciales recibidas
     * @throws Exception si Spring Security no puede obtener el gestor configurado
     */
    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Proporciona el codificador BCrypt utilizado para proteger las contraseñas.
     *
     * @return codificador de contraseñas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
