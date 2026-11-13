package eus.birtlh.g3.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "users_email_key", columnList = "email", unique = true)
        }
)
/**
 * Entidad persistente que representa a un usuario del sistema.
 *
 * <p>La validación de los datos de entrada se realiza en los DTOs. Por ello, el constructor y
 * los métodos que modifican el estado tienen visibilidad de paquete: solo los casos de uso del
 * módulo {@code user} pueden crear o modificar usuarios.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = UserValidationConstraints.MAX_NAME_LENGTH)
    private String name;

    @Column(name = "last_name", length = UserValidationConstraints.MAX_LAST_NAME_LENGTH)
    private String lastName;

    @Column(nullable = false, length = UserValidationConstraints.MAX_EMAIL_LENGTH)
    private String email;

    @Column(name = "password_hash", nullable = false, length = UserValidationConstraints.MAX_PASSWORD_HASH_LENGTH)
    @Getter(AccessLevel.NONE)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea un usuario con los datos ya validados por el DTO correspondiente.
     *
     * @param name nombre del usuario
     * @param lastName apellido del usuario; puede ser {@code null}
     * @param email dirección de correo electrónico del usuario
     * @param passwordHash hash de la contraseña del usuario
     * @param role rol asignado al usuario
     */
    User(
            String name,
            String lastName,
            String email,
            String passwordHash,
            Role role
    ) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir el usuario.
     */
    @PrePersist
    private void onPersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar el usuario persistido.
     */
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Cambia el nombre del usuario.
     *
     * @param name nombre ya validado
     */
    void changeName(String name) {
        this.name = name;
    }

    /**
     * Cambia el apellido del usuario.
     *
     * @param lastName apellido ya validado; puede ser {@code null}
     */
    void changeLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Cambia el correo electrónico del usuario.
     *
     * @param email correo electrónico ya validado
     */
    void changeEmail(String email) {
        this.email = email;
    }

    /**
     * Cambia el hash de la contraseña del usuario.
     *
     * @param passwordHash hash ya generado y validado
     */
    void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Cambia el rol asignado al usuario.
     *
     * @param role rol ya autorizado y validado
     */
    void changeRole(Role role) {
        this.role = role;
    }

    /**
     * Habilita el acceso del usuario al sistema.
     */
    void enable() {
        this.enabled = true;
    }

    /**
     * Deshabilita el acceso del usuario al sistema.
     */
    void disable() {
        this.enabled = false;
    }
}
