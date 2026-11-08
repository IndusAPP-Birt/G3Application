package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.Machine;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad persistente que define la recurrencia de un mantenimiento preventivo para una máquina.
 *
 * <p>Un plan pertenece de forma permanente a una máquina y configura la frecuencia, el margen de
 * aviso y la próxima fecha para la que todavía no se ha generado un preventivo. La validación de
 * los datos de entrada se realiza en los DTOs; por ello, el constructor y los métodos que
 * modifican el estado tienen visibilidad de paquete y solo los casos de uso del módulo
 * {@code preventive} pueden utilizarlos.</p>
 */
@Entity
@Table(name = "preventive_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreventivePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false, updatable = false)
    private Machine machine;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1_000)
    private String description;

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
     * Crea un plan preventivo activo con los datos ya validados por el DTO correspondiente.
     *
     * @param machine máquina a la que pertenece el plan
     * @param name nombre descriptivo del plan
     * @param description instrucciones o descripción del plan; puede ser {@code null}
     * @param intervalDays número de días entre dos preventivos consecutivos
     * @param noticeDays número de días de antelación para generar el preventivo
     * @param nextDueDate próxima fecha para la que aún no se ha generado un preventivo
     */
    PreventivePlan(
            Machine machine,
            String name,
            String description,
            int intervalDays,
            int noticeDays,
            LocalDate nextDueDate
    ) {
        this.machine = machine;
        this.name = name;
        this.description = description;
        this.intervalDays = intervalDays;
        this.noticeDays = noticeDays;
        this.nextDueDate = nextDueDate;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir el plan.
     */
    @PrePersist
    private void onPersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar el plan persistido.
     */
    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Cambia el nombre descriptivo del plan.
     *
     * @param name nombre ya validado
     */
    void changeName(String name) {
        this.name = name;
    }

    /**
     * Cambia las instrucciones o descripción del plan.
     *
     * @param description descripción ya validada; puede ser {@code null}
     */
    void changeDescription(String description) {
        this.description = description;
    }

    /**
     * Cambia la frecuencia del plan.
     *
     * @param intervalDays número de días ya validado entre preventivos consecutivos
     */
    void changeIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    /**
     * Cambia el margen de aviso del plan.
     *
     * @param noticeDays número de días ya validado de antelación para generar el preventivo
     */
    void changeNoticeDays(int noticeDays) {
        this.noticeDays = noticeDays;
    }

    /**
     * Reprograma de forma explícita la próxima fecha pendiente de generación.
     *
     * @param nextDueDate nueva fecha ya validada para el próximo preventivo
     */
    void scheduleNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    /**
     * Avanza la próxima fecha pendiente según la frecuencia del plan.
     *
     * <p>El proceso de planificación invoca este método después de generar el preventivo de la
     * fecha actual, con independencia de que dicho preventivo se complete posteriormente.</p>
     */
    void advanceNextDueDate() {
        nextDueDate = nextDueDate.plusDays(intervalDays);
    }

    /**
     * Activa el plan para que vuelva a generar preventivos.
     */
    void activate() {
        this.active = true;
    }

    /**
     * Desactiva el plan sin eliminar su configuración ni su histórico de preventivos.
     */
    void deactivate() {
        active = false;
    }
}
