package eus.birtlh.g3.machine;

import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.machine.dto.UpdateAssetCodeRequest;
import eus.birtlh.g3.machine.dto.UpdateMachineRequest;
import eus.birtlh.g3.machine.exception.AssetCodeAlreadyInUseException;
import eus.birtlh.g3.machine.exception.SerialNumberAlreadyInUseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MachineServiceTest {

    @Autowired
    private MachineService machineService;

    /** Comprueba que el código patrimonial identifica de forma única a una máquina. */
    @DisplayName("Rechaza crear una máquina con código patrimonial duplicado")
    @Test
    void rechazaCreacionConCodigoPatrimonialDuplicado() {
        createMachine("TEST-MACHINE-ASSET-001", "SN-ASSET-001");

        assertThatThrownBy(() -> createMachine("TEST-MACHINE-ASSET-001", "SN-ASSET-002"))
                .as("No se crea una segunda máquina con el mismo código")
                .isInstanceOf(AssetCodeAlreadyInUseException.class)
                .hasMessage("El código patrimonial TEST-MACHINE-ASSET-001 ya existe.");
    }

    /** Comprueba que el número de serie no puede pertenecer a más de una máquina. */
    @DisplayName("Rechaza crear una máquina con número de serie duplicado")
    @Test
    void rechazaCreacionConNumeroDeSerieDuplicado() {
        createMachine("TEST-MACHINE-SERIAL-001", "SN-SERIAL-001");

        assertThatThrownBy(() -> createMachine("TEST-MACHINE-SERIAL-002", "SN-SERIAL-001"))
                .as("No se crea una segunda máquina con el mismo número de serie")
                .isInstanceOf(SerialNumberAlreadyInUseException.class)
                .hasMessage("El número de serie SN-SERIAL-001 ya existe.");
    }

    /** Comprueba que la corrección de código patrimonial respeta la unicidad entre máquinas. */
    @DisplayName("Rechaza actualizar un código patrimonial que pertenece a otra máquina")
    @Test
    void rechazaActualizacionConCodigoPatrimonialDeOtraMaquina() {
        MachineResponse first = createMachine("TEST-MACHINE-UPDATE-ASSET-001", "SN-UPDATE-ASSET-001");
        MachineResponse second = createMachine("TEST-MACHINE-UPDATE-ASSET-002", "SN-UPDATE-ASSET-002");

        assertThatThrownBy(() -> machineService.updateMachineAssetCode(
                second.id(), new UpdateAssetCodeRequest(first.assetCode())
        ))
                .as("No se reutiliza el código patrimonial de otra máquina")
                .isInstanceOf(AssetCodeAlreadyInUseException.class)
                .hasMessage("El código patrimonial TEST-MACHINE-UPDATE-ASSET-001 ya existe.");
    }

    /** Comprueba que una actualización ordinaria no puede reutilizar el número de serie de otra máquina. */
    @DisplayName("Rechaza actualizar un número de serie que pertenece a otra máquina")
    @Test
    void rechazaActualizacionConNumeroDeSerieDeOtraMaquina() {
        MachineResponse first = createMachine("TEST-MACHINE-UPDATE-SERIAL-001", "SN-UPDATE-SERIAL-001");
        MachineResponse second = createMachine("TEST-MACHINE-UPDATE-SERIAL-002", "SN-UPDATE-SERIAL-002");

        assertThatThrownBy(() -> machineService.updateMachine(
                second.id(),
                new UpdateMachineRequest(
                        second.name(), second.manufacturer(), second.model(), first.serialNumber(),
                        second.location(), MachineStatus.ACTIVE
                )
        ))
                .as("No se reutiliza el número de serie de otra máquina")
                .isInstanceOf(SerialNumberAlreadyInUseException.class)
                .hasMessage("El número de serie SN-UPDATE-SERIAL-001 ya existe.");
    }

    private MachineResponse createMachine(String assetCode, String serialNumber) {
        return machineService.createMachine(new CreateMachineRequest(
                assetCode, "Máquina de prueba", "Fabricante", "Modelo", serialNumber,
                "Laboratorio", LocalDate.of(2030, 1, 1)
        ));
    }
}
