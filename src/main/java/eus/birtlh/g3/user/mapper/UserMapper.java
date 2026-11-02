package eus.birtlh.g3.user.mapper;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.UserResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

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
