package eus.birtlh.g3.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador de {@link User} al contrato de seguridad de Spring Security.
 *
 * <p>Expone el email como identidad, el rol como autoridad y el estado de acceso del usuario. El
 * hash de contraseña solo se entrega internamente a Spring Security para su verificación y no debe
 * serializarse ni incluirse en registros.</p>
 */
@NullMarked
public final class UserPrincipal implements UserDetails {

    @Getter private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;

    private UserPrincipal(
            Long id,
            String email,
            String passwordHash,
            Role role,
            boolean enabled
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    /**
     * Crea el principal de seguridad a partir de una entidad de usuario persistida.
     *
     * @param user usuario del que se obtienen los datos de autenticación y autorización
     * @return principal preparado para Spring Security
     */
    static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHashForAuthentication(),
                user.getRole(),
                user.isEnabled()
        );
    }

    /**
     * Devuelve el único rol asignado al usuario.
     *
     * @return rol del usuario
     */
    public Role getRole() {
        return role;
    }

    /**
     * Devuelve la autoridad correspondiente al rol asignado al usuario.
     *
     * @return colección inmutable con la autoridad del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Devuelve el hash de contraseña únicamente para que Spring Security realice su comprobación.
     *
     * @return hash BCrypt de la contraseña
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Devuelve el email que identifica al usuario durante la autenticación.
     *
     * @return email normalizado del usuario
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si el usuario puede autenticarse en la aplicación.
     *
     * @return {@code true} si el usuario está habilitado; en caso contrario, {@code false}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
