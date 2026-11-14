package eus.birtlh.g3.auth.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Convierte el claim {@code role} de un JWT en la autoridad de Spring Security correspondiente.
 *
 * <p>El dominio asigna un único rol por usuario, por lo que el conversor devuelve una colección
 * con cero o una autoridad. No establece una jerarquía entre roles.</p>
 */
@Component
@NullMarked
public class JwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Convierte el rol único incluido en el token en una autoridad de Spring Security.
     *
     * @param jwt token validado del que se obtiene el claim de rol
     * @return colección con la autoridad del rol o vacía si el claim no está presente
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String role = jwt.getClaimAsString("role");

        if (role == null) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority(role));
    }
}
