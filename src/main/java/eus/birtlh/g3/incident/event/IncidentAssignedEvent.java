package eus.birtlh.g3.incident.event;

public record IncidentAssignedEvent(
        long incidentId,
        long maintenanceTechnicianId
) {
}
