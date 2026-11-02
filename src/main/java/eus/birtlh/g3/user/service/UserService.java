package eus.birtlh.g3.user.service;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación que coordina los casos de uso de consulta de usuarios.
 *
 * <p>Obtiene entidades {@link User} desde el repositorio y las transforma en DTOs públicos antes
 * de devolverlas a la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Obtiene todos los usuarios registrados como DTOs de respuesta.
     *
     * @return lista de usuarios públicos; puede estar vacía
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

}
