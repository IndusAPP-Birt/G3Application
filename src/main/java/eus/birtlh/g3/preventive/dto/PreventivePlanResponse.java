package eus.birtlh.g3.preventive.dto;

import eus.birtlh.g3.machine.dto.MachineResponse;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Datos públicos de un plan preventivo persistido, expuestos por la API.
 *
 * <p>Incluye la representación completa de la máquina a la que pertenece el plan. Esta decisión
 * permite identificar y consultar su contexto técnico en una única respuesta, sin introducir
 * ciclos porque {@link MachineResponse} no incorpora planes preventivos.</p>
 *
 * @param id identificador único del plan preventivo
 * @param machine máquina industrial a la que pertenece el plan
 * @param name nombre descriptivo del plan
 * @param description instrucciones o descripción del plan; puede ser {@code null}
 * @param intervalDays número de días entre preventivos consecutivos
 * @param noticeDays número de días de antelación para generar un preventivo
 * @param nextDueDate próxima fecha para la que todavía no se ha generado un preventivo
 * @param active indica si el plan puede generar nuevos preventivos
 * @param createdAt fecha de creación del plan
 * @param updatedAt fecha de última actualización del plan
 */
public record PreventivePlanResponse(
        long id,
        MachineResponse machine,
        String name,
        String description,
        int intervalDays,
        int noticeDays,
        LocalDate nextDueDate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
