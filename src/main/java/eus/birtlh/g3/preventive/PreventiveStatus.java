package eus.birtlh.g3.preventive;

/**
 * Estados del ciclo de trabajo de un preventivo concreto.
 *
 * <p>Un preventivo se crea en estado {@link #PENDING}, se asigna a un inspector, se inicia y se
 * completa. El nombre de cada constante se persiste como texto en la columna de estado del
 * preventivo.</p>
 */
public enum PreventiveStatus {
    // TODO: Incorporar BLOCKED cuando se implemente la gestión de incidencias.
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED
}
