package eus.birtlh.g3.incident;

import eus.birtlh.g3.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Registro inmutable de una actuación realizada para resolver una incidencia.
 *
 * <p>Cada acción conserva el trabajo realizado, el técnico que la registró y su fecha de
 * creación. Una incidencia puede tener varias acciones durante su reparación.</p>
 */
@Entity
@Table(
        name = "incident_actions",
        indexes = @Index(
                name = "incident_actions_incident_created_key",
                columnList = "incident_id, created_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, updatable = false)
    private Incident incident;

    @Column(nullable = false, length = 2_000)
    private String workPerformed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_technician_id", nullable = false, updatable = false)
    private User performedByTechnician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Crea una acción de trabajo asociada a una incidencia.
     *
     * @param incident incidencia sobre la que se realiza la actuación
     * @param workPerformed trabajo realizado por el técnico
     * @param performedByTechnician técnico que registra la actuación
     */
    IncidentAction(Incident incident, String workPerformed, User performedByTechnician) {
        this.incident = incident;
        this.workPerformed = workPerformed;
        this.performedByTechnician = performedByTechnician;
    }

    /**
     * Establece la fecha de creación antes de persistir la acción.
     */
    @PrePersist
    private void onPersist() {
        createdAt = Instant.now();
    }
}
