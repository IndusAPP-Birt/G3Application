package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.Machine;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad persistente que asigna una plantilla preventiva a una máquina y configura su
 * planificación.
 *
 * <p>Una misma plantilla puede asignarse a varios activos y una máquina puede recibir varias
 * plantillas. La combinación de plan y máquina es única. Las dos relaciones son inmutables;
 * solo se actualizan el calendario y la activación de la asignación.</p>
 */
@Entity
@Table(
        name = "preventive_plan_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "preventive_plan_assignments_plan_machine_key",
                columnNames = {"preventive_plan_id", "machine_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreventivePlanAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preventive_plan_id", nullable = false, updatable = false)
    private PreventivePlan preventivePlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false, updatable = false)
    private Machine machine;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "notice_days", nullable = false)
    private int noticeDays;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea una asignación preventiva activa con los datos ya validados por el DTO
     * correspondiente.
     *
     * @param preventivePlan plantilla preventiva que se asigna
     * @param machine máquina a la que se aplica la plantilla
     * @param intervalDays número de días entre mantenimientos preventivos consecutivos
     * @param noticeDays días de antelación para generar el mantenimiento preventivo
     * @param nextDueDate próxima fecha pendiente de generación
     */
    PreventivePlanAssignment(
            PreventivePlan preventivePlan,
            Machine machine,
            int intervalDays,
            int noticeDays,
            LocalDate nextDueDate
    ) {
        this.preventivePlan = preventivePlan;
        this.machine = machine;
        this.intervalDays = intervalDays;
        this.noticeDays = noticeDays;
        this.nextDueDate = nextDueDate;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir la
     * asignación.
     */
    @PrePersist
    private void onPersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar la asignación persistida.
     */
    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Cambia la frecuencia de la planificación.
     *
     * @param intervalDays número de días ya validado entre mantenimientos consecutivos
     */
    void changeIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    /**
     * Cambia el margen de aviso de la planificación.
     *
     * @param noticeDays número de días ya validado de antelación para la generación
     */
    void changeNoticeDays(int noticeDays) {
        this.noticeDays = noticeDays;
    }

    /**
     * Reprograma de forma explícita la próxima fecha pendiente de generación.
     *
     * @param nextDueDate nueva fecha ya validada para el próximo mantenimiento preventivo
     */
    void scheduleNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    /**
     * Avanza la próxima fecha pendiente según la frecuencia configurada.
     *
     * <p>El proceso de generación invoca este método después de crear el mantenimiento previsto
     * para la fecha actual.</p>
     */
    void advanceNextDueDate() {
        nextDueDate = nextDueDate.plusDays(intervalDays);
    }

    /**
     * Activa la asignación para que vuelva a generar mantenimientos preventivos.
     */
    void activate() {
        this.active = true;
    }

    /**
     * Desactiva la asignación sin eliminar su configuración ni los mantenimientos ya generados.
     */
    void deactivate() {
        this.active = false;
    }
}
