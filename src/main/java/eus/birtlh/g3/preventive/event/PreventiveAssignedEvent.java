package eus.birtlh.g3.preventive.event;

/**
 * Evento de dominio publicado cuando se asigna un inspector a un preventivo.
 *
 * <p>Transporta únicamente los identificadores necesarios para que los consumidores resuelvan
 * sus propios datos sin acoplar el módulo preventivo a un canal de notificación concreto.</p>
 *
 * @param preventiveId identificador del preventivo asignado
 * @param inspectorId identificador del inspector que recibe la asignación
 */
public record PreventiveAssignedEvent(
        long preventiveId,
        long inspectorId
) {
}
