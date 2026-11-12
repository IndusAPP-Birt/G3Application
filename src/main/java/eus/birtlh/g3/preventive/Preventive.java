package eus.birtlh.g3.preventive;

import eus.birtlh.g3.preventive.exceptions.PreventiveWorkReportRequiredException;
import eus.birtlh.g3.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Ejecución concreta de una asignación de plantilla preventiva a una máquina.
 *
 * <p>Conserva una instantánea del nombre y la descripción de la plantilla para preservar el
 * contexto histórico de la ejecución. Su ciclo de trabajo avanza de pendiente a asignado, en
 * progreso y completado.</p>
 */
@Entity
@Table(
        name = "preventives",
        indexes = {
                @Index(
                        name = "preventives_assignment_due_date_key",
                        columnList = "preventive_plan_assignment_id, due_date",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Preventive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preventive_plan_assignment_id", nullable = false, updatable = false)
    private PreventivePlanAssignment preventivePlanAssignment;

    @Column(name = "plan_name", nullable = false, length = 150, updatable = false)
    private String planName;

    @Column(name = "plan_description", length = 1_000, updatable = false)
    private String planDescription;

    @Column(name = "due_date", nullable = false, updatable = false)
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_inspector_id")
    private User assignedInspector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreventiveStatus status = PreventiveStatus.PENDING;

    @Column(name = "work_performed", length = 2_000)
    private String workPerformed;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea un preventivo pendiente desde una asignación y una fecha prevista de ejecución.
     *
     * <p>Conserva una copia del nombre y la descripción de la plantilla para preservar el
     * contexto histórico de la ejecución.</p>
     *
     * @param preventivePlanAssignment asignación de plantilla y máquina que origina el preventivo
     * @param dueDate fecha prevista inmutable para realizar el preventivo
     */
    Preventive(PreventivePlanAssignment preventivePlanAssignment, LocalDate dueDate) {
        this.preventivePlanAssignment = preventivePlanAssignment;
        this.planName = preventivePlanAssignment.getPreventivePlan().getName();
        this.planDescription = preventivePlanAssignment.getPreventivePlan().getDescription();
        this.dueDate = dueDate;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir el
     * preventivo.
     */
    @PrePersist
    private void onPersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar el preventivo persistido.
     */
    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Asigna el preventivo a un inspector y registra el momento de la asignación.
     *
     * @param inspector usuario inspector ya autorizado y validado
     */
    void assignInspector(User inspector) {
        assignedInspector = inspector;
        assignedAt = Instant.now();
        status = PreventiveStatus.ASSIGNED;
    }

    /**
     * Registra el inicio de la ejecución del preventivo.
     */
    void start() {
        startedAt = Instant.now();
        status = PreventiveStatus.IN_PROGRESS;
    }

    /**
     * Registra o corrige el trabajo realizado durante la ejecución del preventivo.
     *
     * @param workPerformed informe de trabajo ya validado y normalizado
     */
    void recordWorkPerformed(String workPerformed) {
        this.workPerformed = workPerformed;
    }

    /**
     * Registra la finalización satisfactoria del preventivo.
     *
     * @throws PreventiveWorkReportRequiredException si no se ha registrado el trabajo realizado
     */
    void complete() {
        if (workPerformed == null || workPerformed.isBlank()) {
            throw new PreventiveWorkReportRequiredException(
                    "Debe registrar el trabajo realizado antes de completar el preventivo."
            );
        }

        completedAt = Instant.now();
        status = PreventiveStatus.COMPLETED;
    }
}
