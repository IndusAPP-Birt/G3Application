package eus.birtlh.g3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de autorización HTTP de la aplicación.
 *
 * <p>Mientras se implementa la autenticación, permite temporalmente el acceso público a la
 * consulta, creación y actualización de usuarios, y exige autenticación para el resto de
 * rutas.</p>
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").permitAll()
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
