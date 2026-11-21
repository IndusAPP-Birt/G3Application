package eus.birtlh.g3.incident;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Incidencia detectada durante la ejecución de un mantenimiento preventivo.
 *
 * <p>La incidencia identifica el problema comunicado por el inspector, su gravedad y el técnico
 * responsable de resolverlo. Su ciclo de trabajo avanza de abierta a asignada, en progreso,
 * resuelta y cerrada.</p>
 */
@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(name = "incidents_preventive_key", columnList = "preventive_id"),
                @Index(
                        name = "incidents_technician_status_key",
                        columnList = "assigned_technician_id, status"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preventive_id", nullable = false, updatable = false)
    private Preventive preventive;

    @Column(nullable = false, length = 2_000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_inspector_id", nullable = false, updatable = false)
    private User reportedByInspector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private User assignedTechnician;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea una incidencia abierta asociada a un preventivo en ejecución.
     *
     * @param preventive preventivo en el que se detecta la incidencia
     * @param description descripción del problema detectado
     * @param severity gravedad asignada a la incidencia
     * @param reportedByInspector inspector que comunica la incidencia
     */
    Incident(Preventive preventive, String description, IncidentSeverity severity, User reportedByInspector) {
        this.preventive = preventive;
        this.description = description;
        this.severity = severity;
        this.reportedByInspector = reportedByInspector;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir la incidencia.
     */
    @PrePersist
    private void onPersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar la incidencia persistida.
     */
    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Asigna la incidencia a un técnico de mantenimiento y registra el momento de la asignación.
     *
     * @param technician técnico ya autorizado y validado
     */
    void assignTechnician(User technician) {
        assignedTechnician = technician;
        assignedAt = Instant.now();
        status = IncidentStatus.ASSIGNED;
    }

    /**
     * Registra el inicio de la reparación de la incidencia.
     */
    void start() {
        startedAt = Instant.now();
        status = IncidentStatus.IN_PROGRESS;
    }

    /**
     * Registra la resolución comunicada por el técnico asignado.
     */
    void resolve() {
        resolvedAt = Instant.now();
        status = IncidentStatus.RESOLVED;
    }

    /**
     * Registra el cierre de la incidencia tras su validación por el inspector.
     */
    void close() {
        closedAt = Instant.now();
        status = IncidentStatus.CLOSED;
    }
}
