package eus.birtlh.g3.user;

/**
 * Roles que se pueden asignar a un {@link User}.
 *
 * <p>El nombre de cada constante se persiste como texto en la columna de rol del usuario.</p>
 */
public enum Role {
    ROLE_ADMINISTRATOR,
    ROLE_INSPECTOR,
    ROLE_MAINTENANCE_TECHNICIAN
}
