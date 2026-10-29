package eus.birtlh.g3.user.repository;

import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a los datos de {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Indica si ya existe un usuario con la dirección de correo electrónico indicada.
     *
     * @param email dirección de correo electrónico que se va a comprobar
     * @return {@code true} si ya existe un usuario con ese correo
     */
    boolean existsByEmail(String email);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo electrónico que se va a buscar
     * @return usuario encontrado o {@link Optional#empty()} si no existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Obtiene los usuarios que tienen asignado un rol determinado.
     *
     * @param role rol por el que se va a filtrar
     * @return lista de usuarios con el rol indicado
     */
    List<User> findAllByRole(Role role);

    /**
     * Obtiene los usuarios según su estado de habilitación.
     *
     * @param enabled estado de habilitación por el que se va a filtrar
     * @return lista de usuarios con el estado indicado
     */
    List<User> findAllByEnabled(boolean enabled);
}
