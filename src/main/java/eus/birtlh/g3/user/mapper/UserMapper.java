package eus.birtlh.g3.user.mapper;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.UserResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Convierte entidades {@link User} en sus representaciones de respuesta para la API.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    /**
     * Convierte un usuario en un DTO de respuesta sin exponer el hash de la contraseña.
     *
     * @param user usuario que se va a convertir
     * @return representación del usuario para la API
     * @throws IllegalArgumentException si el usuario es {@code null}
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
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
