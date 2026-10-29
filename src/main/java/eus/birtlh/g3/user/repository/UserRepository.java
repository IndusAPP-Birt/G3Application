package eus.birtlh.g3.user.repository;

import eus.birtlh.g3.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a los datos de {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
