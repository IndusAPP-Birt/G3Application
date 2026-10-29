package eus.birtlh.g3.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import static eus.birtlh.g3.user.UserValidationConstraints.MAX_NAME_LENGTH;

/**
 * Entidad que representa a un usuario de la aplicación.
 *
 * <p>Centraliza los datos del usuario y las reglas de dominio asociadas a sus
 * modificaciones.</p>
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "users_email_key", columnList = "email", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(UserValidationConstraints.EMAIL_REGEX);

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del usuario. */
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    /** Apellidos del usuario. */
    @Column(name = "last_name", length = MAX_NAME_LENGTH)
    private String lastName;

    /** Dirección de correo electrónico del usuario. */
    // TODO: En la capa de servicio, comprobar que el correo no está en uso al crear o modificar
    //  un usuario, y gestionar la posible violación del índice único ante operaciones concurrentes.
    @Column(nullable = false, length = UserValidationConstraints.MAX_EMAIL_LENGTH)
    private String email;

    /** Hash de la contraseña del usuario. */
    // TODO: En la capa de servicio, generar este valor con PasswordEncoder antes de crear o
    //  modificar un usuario. Nunca debe recibirse una contraseña en texto plano.
    @Column(name = "password_hash", nullable = false)
    @Getter(AccessLevel.NONE)
    private String passwordHash;

    /** Rol asignado al usuario. */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /** Indica si el usuario puede acceder a la aplicación. */
    @Column(nullable = false)
    private boolean enabled = true;

    /** Fecha y hora de creación del usuario. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Fecha y hora de la última actualización del usuario. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea un usuario con sus datos iniciales.
     *
     * @param name nombre del usuario
     * @param lastName apellidos del usuario; pueden ser {@code null}
     * @param email dirección de correo electrónico del usuario
     * @param passwordHash hash de contraseña ya generado; nunca la contraseña en texto plano
     * @param role rol asignado al usuario
     * @throws IllegalArgumentException si alguno de los datos no cumple las invariantes de dominio
     */
    public User(
            String name,
            String lastName,
            String email,
            String passwordHash,
            Role role
    ) {
        this.name = normalizeAndValidateName(name);
        this.lastName = normalizeAndValidateLastName(lastName);
        this.email = normalizeAndValidateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.role = validateRole(role);
    }

    /**
     * Inicializa las fechas de creación y actualización antes de persistir el usuario.
     */
    @PrePersist
    public void onPersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Actualiza la fecha de modificación antes de actualizar el usuario.
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Cambia el nombre del usuario.
     *
     * @param name nuevo nombre del usuario
     * @throws IllegalArgumentException si el nombre no cumple las invariantes de dominio
     */
    public void changeName(String name) {
        this.name = normalizeAndValidateName(name);
    }

    /**
     * Cambia los apellidos del usuario.
     *
     * @param lastName nuevos apellidos del usuario; pueden ser {@code null}
     * @throws IllegalArgumentException si los apellidos no cumplen las invariantes de dominio
     */
    public void changeLastName(String lastName) {
        this.lastName = normalizeAndValidateLastName(lastName);
    }

    /**
     * Cambia la dirección de correo electrónico del usuario.
     *
     * @param email nueva dirección de correo electrónico del usuario
     * @throws IllegalArgumentException si el correo no cumple las invariantes de dominio
     */
    public void changeEmail(String email) {
        this.email = normalizeAndValidateEmail(email);
    }

    /**
     * Cambia el hash de la contraseña del usuario.
     *
     * @param passwordHash nuevo hash de contraseña, nunca la contraseña en texto plano
     * @throws IllegalArgumentException si el hash no cumple las invariantes de dominio
     */
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    /**
     * Cambia el rol asignado al usuario.
     *
     * @param role nuevo rol del usuario
     * @throws IllegalArgumentException si el rol no cumple las invariantes de dominio
     * @throws IllegalStateException si se intenta rebajar el rol de un administrador
     */
    // TODO: Autorizar este caso de uso en la capa de servicio, por ejemplo con
    //  @PreAuthorize("hasRole('ADMINISTRATOR')"). La entidad no conoce al usuario autenticado.
    public void changeRole(Role role) {
        Role validatedRole = validateRole(role);

        if (this.role == Role.ROLE_ADMINISTRATOR && validatedRole != Role.ROLE_ADMINISTRATOR) {
            throw new IllegalStateException("El administrador no puede rebajarse de rol.");
        }

        this.role = validatedRole;
    }

    /**
     * Habilita al usuario.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Deshabilita al usuario.
     *
     * @throws IllegalStateException si se intenta deshabilitar a un administrador
     */
    public void disable() {
        if (this.role == Role.ROLE_ADMINISTRATOR) {
            throw new IllegalStateException("El administrador no puede deshabilitarse.");
        }

        this.enabled = false;
    }

    /**
     * Normaliza y valida el nombre de un usuario.
     *
     * @param name nombre que se va a normalizar y validar
     * @return nombre sin espacios en los extremos
     * @throws IllegalArgumentException si el nombre es nulo, está vacío o no cumple la longitud permitida
     */
    private String normalizeAndValidateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        String normalizedName = name.strip();

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (normalizedName.length() < UserValidationConstraints.MIN_NAME_LENGTH || normalizedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 100 caracteres.");
        }

        return normalizedName;
    }

    /**
     * Normaliza y valida los apellidos de un usuario.
     *
     * @param lastName apellidos que se van a normalizar y validar
     * @return apellidos sin espacios en los extremos, o {@code null} si no se indican
     * @throws IllegalArgumentException si los apellidos indicados están vacíos o no cumplen la longitud permitida
     */
    private String normalizeAndValidateLastName(String lastName) {
        if (lastName == null) {
            return null;
        }

        String normalizedLastName = lastName.strip();

        if (normalizedLastName.isBlank()) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos.");
        }

        if (normalizedLastName.length() < UserValidationConstraints.MIN_NAME_LENGTH || normalizedLastName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Los apellidos, si se indican, deben tener entre 2 y 100 caracteres.");
        }

        return normalizedLastName;
    }

    /**
     * Normaliza y valida la dirección de correo electrónico de un usuario.
     *
     * @param email dirección de correo electrónico que se va a normalizar y validar
     * @return dirección de correo electrónico sin espacios en los extremos y en minúsculas
     * @throws IllegalArgumentException si el correo es nulo, está vacío, supera la longitud permitida
     *                                  o no tiene un formato válido
     */
    private String normalizeAndValidateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("La dirección de correo electrónico es obligatoria.");
        }

        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("La dirección de correo electrónico es obligatoria.");
        }

        if (normalizedEmail.length() > UserValidationConstraints.MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("La dirección de correo electrónico no puede superar los 254 caracteres.");
        }

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("La dirección de correo electrónico debe tener un formato válido.");
        }

        return normalizedEmail;
    }

    /**
     * Valida el hash de la contraseña de un usuario.
     *
     * @param passwordHash hash de contraseña que se va a validar
     * @return hash de contraseña validado
     * @throws IllegalArgumentException si el hash es nulo, está vacío o supera la longitud permitida
     */
    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña es obligatorio.");
        }

        if (passwordHash.length() > UserValidationConstraints.MAX_PASSWORD_HASH_LENGTH) {
            throw new IllegalArgumentException("El hash de la contraseña no puede superar los 255 caracteres.");
        }

        return passwordHash;
    }

    /**
     * Valida el rol asignado a un usuario.
     *
     * @param role rol que se va a validar
     * @return rol validado
     * @throws IllegalArgumentException si el rol es nulo
     */
    private Role validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        return role;
    }
}
