package eus.birtlh.g3.preventive;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad persistente que representa una plantilla de mantenimiento preventivo.
 *
 * <p>El plan es independiente de los activos y contiene el nombre y las acciones preventivas
 * descritas que deben aplicarse. La asignación a una máquina, su planificación y la generación
 * de mantenimientos concretos corresponden a entidades y casos de uso separados. La validación
 * de entrada se realiza en los DTOs; por ello, el constructor y los métodos que modifican el
 * estado tienen visibilidad de paquete y solo los casos de uso del módulo {@code preventive}
 * pueden utilizarlos.</p>
 */
@Entity
@Table(name = "preventive_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreventivePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1_000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea una plantilla preventiva activa con los datos ya validados por el DTO correspondiente.
     *
     * @param name nombre descriptivo de la plantilla
     * @param description acciones preventivas o instrucciones; puede ser {@code null}
     */
    PreventivePlan(String name, String description) {
        this.name = name;
        this.description = description;
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
     * Activa la plantilla para que pueda asignarse a activos.
     */
    void activate() {
        this.active = true;
    }

    /**
     * Desactiva la plantilla sin eliminar su configuración ni afectar a los mantenimientos ya
     * generados.
     */
    void deactivate() {
        active = false;
    }
}
