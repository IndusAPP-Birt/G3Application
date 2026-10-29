package eus.birtlh.g3.user.repository;

import eus.birtlh.g3.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
