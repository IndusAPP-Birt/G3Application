package eus.birtlh.g3.incident;

/**
 * Estados del ciclo de trabajo de una incidencia.
 *
 * <p>Una incidencia se crea en estado {@link #OPEN}, se asigna a un técnico, se inicia, se
 * resuelve y se cierra. El nombre de cada constante se persiste como texto en la columna de
 * estado de la incidencia.</p>
 */
public enum IncidentStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
