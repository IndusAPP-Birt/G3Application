package eus.birtlh.g3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de autorización HTTP de la aplicación.
 *
 * <p>Mientras se implementa la autenticación, permite el acceso público al listado de usuarios
 * y exige autenticación para el resto de rutas.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Construye la cadena de filtros de seguridad de la aplicación.
     *
     * @param http configurador de seguridad HTTP proporcionado por Spring Security
     * @return cadena de filtros con las reglas de autorización configuradas
     * @throws Exception si Spring Security no puede construir la cadena de filtros
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
