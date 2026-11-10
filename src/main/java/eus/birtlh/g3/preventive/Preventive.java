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
 * Entidad persistente que representa una ejecución concreta de un {@link PreventivePlan}.
 *
 * <p>El preventivo conserva de forma inmutable el plan de origen y su fecha prevista. Se crea en
 * estado {@link PreventiveStatus#PENDING}, puede asignarse a un inspector, iniciarse y
 * completarse tras registrar el trabajo realizado. La validación de las transiciones y la
 * comprobación de que el usuario asignado tiene rol de inspector corresponden a los casos de uso
 * del módulo {@code preventive}.</p>
 */
@Entity
@Table(
        name = "preventives",
        indexes = {
                @Index(
                        name = "preventives_plan_due_date_key",
                        columnList = "preventive_plan_id, due_date",
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
    @JoinColumn(name = "preventive_plan_id", nullable = false, updatable = false)
    private PreventivePlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_inspector_id")
    private User assignedInspector;

    @Column(name = "due_date", nullable = false, updatable = false)
    private LocalDate dueDate;

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
     * Crea un preventivo pendiente a partir de un plan y una fecha prevista de ejecución.
     *
     * @param plan plan preventivo del que procede esta ejecución
     * @param dueDate fecha prevista inmutable para realizar el preventivo
     */
    Preventive(PreventivePlan plan, LocalDate dueDate) {
        this.plan = plan;
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
