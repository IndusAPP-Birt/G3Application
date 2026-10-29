package eus.birtlh.g3.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura la seguridad HTTP y los componentes criptográficos de la aplicación.
 */
@Configuration
public class SecurityConfig {

    // TODO: Al implementar JWT, configurar la autenticación, las reglas de autorización por rol
    //  y la política de CSRF adecuada para el mecanismo de autenticación elegido.
    /**
     * Define la cadena de filtros de seguridad y las reglas de acceso a los endpoints.
     *
     * @param http configurador de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si no se puede construir la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/roles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
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
