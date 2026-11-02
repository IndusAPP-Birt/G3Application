package eus.birtlh.g3.user.mapper;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.UserResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link User} persistidas en sus DTOs públicos de respuesta.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    /**
     * Transforma un usuario persistido en su representación pública.
     *
     * <p>El hash de contraseña no se incluye en el DTO resultante.</p>
     *
     * @param user usuario que se desea exponer
     * @return DTO público correspondiente al usuario
     * @throws IllegalArgumentException si el usuario es {@code null} o no está persistido
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException("El usuario debe estar persistido.");
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                RoleMapper.toResponse(user.getRole()),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
