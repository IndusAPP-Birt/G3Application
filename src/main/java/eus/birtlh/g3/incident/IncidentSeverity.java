package eus.birtlh.g3.incident;

/**
 * Grados de gravedad que puede tener una incidencia.
 *
 * <p>La gravedad se establece al crear la incidencia y el nombre de cada constante se persiste
 * como texto en la columna de gravedad.</p>
 */
public enum IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
