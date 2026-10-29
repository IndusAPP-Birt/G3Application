package eus.birtlh.g3.user.service;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestiona las operaciones de negocio relacionadas con los usuarios.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios registrados
     */
    // TODO: Autorizar este caso de uso antes de exponerlo mediante un endpoint, ya que devuelve emails.
    // TODO: Definir el orden del listado y añadir paginación si el número de usuarios crece.
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
