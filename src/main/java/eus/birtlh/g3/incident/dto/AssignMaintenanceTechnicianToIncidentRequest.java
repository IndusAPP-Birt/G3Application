package eus.birtlh.g3.incident.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignMaintenanceTechnicianToIncidentRequest(
        @NotNull(message = "El técnico de mantenimiento es obligatorio.")
        @Positive(message = "El identificador debe ser mayor que cero.")
        Long maintenanceTechnicianId
) {
}
