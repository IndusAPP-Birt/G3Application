package eus.birtlh.g3.machine;

/**
 * Estados operativos que se pueden asignar a una {@link Machine}.
 *
 * <p>El nombre de cada constante se persiste como texto en la columna de estado de la máquina.</p>
 */
public enum MachineStatus {
    ACTIVE,
    INACTIVE,
    RETIRED
}
