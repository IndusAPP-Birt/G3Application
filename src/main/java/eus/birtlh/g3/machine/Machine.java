package eus.birtlh.g3.machine;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad persistente que representa una máquina industrial registrada en el sistema.
 *
 * <p>La validación de los datos de entrada se realiza en los DTOs. Por ello, el constructor y
 * los métodos que modifican el estado tienen visibilidad de paquete: solo los casos de uso del
 * módulo {@code machine} pueden crear o modificar máquinas. La fecha de puesta en servicio es
 * inmutable una vez creada la entidad; el código patrimonial puede corregirse mediante un caso
 * de uso específico.</p>
 */
@Entity
@Table(
        name = "machines",
        indexes = {
                @Index(
                        name = "machines_asset_code_key",
                        columnList = "asset_code",
                        unique = true
                ),
                @Index(
                        name = "machines_serial_number_key",
                        columnList = "serial_number",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 150)
    private String location;

    @Column(name = "commissioned_at", nullable = false, updatable = false)
    private LocalDate commissionedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MachineStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Crea una máquina industrial con los datos ya validados por el DTO correspondiente.
     *
     * <p>Las máquinas se registran inicialmente en estado {@link MachineStatus#ACTIVE}.</p>
     *
     * @param assetCode código patrimonial único de la máquina
     * @param name nombre descriptivo de la máquina
     * @param manufacturer fabricante de la máquina; puede ser {@code null}
     * @param model modelo de la máquina; puede ser {@code null}
     * @param serialNumber número de serie de la máquina; puede ser {@code null}
     * @param location ubicación de la máquina; puede ser {@code null}
     * @param commissionedAt fecha de puesta en servicio de la máquina
     */
    Machine(
            String assetCode,
            String name,
            String manufacturer,
            String model,
            String serialNumber,
            String location,
            LocalDate commissionedAt
    ) {
        this.assetCode = assetCode;
        this.name = name;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.location = location;
        this.commissionedAt = commissionedAt;
        this.status = MachineStatus.ACTIVE;
    }

    /**
     * Establece las fechas de creación y de última actualización antes de persistir la máquina.
     */
    @PrePersist
    private void onPersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Actualiza la fecha de última modificación antes de actualizar la máquina persistida.
     */
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Corrige el código patrimonial de la máquina.
     *
     * <p>Este cambio se realiza mediante un caso de uso independiente para evitar mezclar una
     * corrección de identificación con la actualización ordinaria de los datos operativos.</p>
     *
     * @param assetCode código patrimonial único ya validado
     */
    void changeAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    /**
     * Cambia el nombre descriptivo de la máquina.
     *
     * @param name nombre ya validado
     */
    void changeName(String name) {
        this.name = name;
    }

    /**
     * Cambia el fabricante de la máquina.
     *
     * @param manufacturer fabricante ya validado; puede ser {@code null}
     */
    void changeManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Cambia el modelo de la máquina.
     *
     * @param model modelo ya validado; puede ser {@code null}
     */
    void changeModel(String model) {
        this.model = model;
    }

    /**
     * Cambia el número de serie de la máquina.
     *
     * @param serialNumber número de serie ya validado; puede ser {@code null}
     */
    void changeSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Cambia la ubicación de la máquina.
     *
     * @param location ubicación ya validada; puede ser {@code null}
     */
    void changeLocation(String location) {
        this.location = location;
    }

    /**
     * Cambia el estado operativo de la máquina.
     *
     * @param status estado ya validado que se desea asignar
     */
    void changeStatus(MachineStatus status) {
        this.status = status;
    }
}
