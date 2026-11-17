package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.MachineStatus;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.machine.dto.UpdateMachineRequest;
import eus.birtlh.g3.machine.exception.MachineNotActiveException;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.assignment.PreventivePlanAssignmentResponse;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.dto.plan.UpdatePreventivePlanRequest;
import eus.birtlh.g3.preventive.exceptions.assignment.PreventivePlanAssignmentAlreadyExistsException;
import eus.birtlh.g3.preventive.exceptions.plan.PreventivePlanInactiveException;
import eus.birtlh.g3.preventive.repository.PreventivePlanAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class PreventivePlanAssignmentServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
    private static final LocalDate NEXT_DUE_DATE = TODAY.plusDays(30);

    @Autowired
    MachineService machineService;

    @Autowired
    PreventivePlanService preventivePlanService;

    @Autowired
    PreventivePlanAssignmentService preventivePlanAssignmentService;

    @Autowired
    PreventivePlanAssignmentRepository preventivePlanAssignmentRepository;

    /**
     * Comprueba que una plantilla activa puede asignarse a una máquina activa con la planificación
     * indicada.
     */
    @DisplayName("Se crea una asignación con plantilla y máquina activas")
    @Test
    void creaAsignacionCuandoLaPlantillaYLaMaquinaEstanActivas() {

        // Creamos la máquina activa
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-ASSIGNMENT-ACTIVE-001",
                        "Máquina de prueba activa",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-ASSIGNMENT-ACTIVE-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos la plantilla preventiva activa
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba activo",
                        "Instrucciones para probar una asignación preventiva válida."
                )
        );

        // Creamos la asignación con su planificación inicial
        PreventivePlanAssignmentResponse assignment =
                preventivePlanAssignmentService.createPreventivePlanAssignment(
                        new CreatePreventivePlanAssignmentRequest(
                                preventivePlan.id(),
                                machine.id(),
                                30,
                                7,
                                NEXT_DUE_DATE
                        )
                );

        // Comprobamos que la asignación conserva las relaciones y la planificación configuradas
        assertThat(assignment.id())
                .as("La asignación recibe un identificador")
                .isPositive();
        assertThat(assignment.preventivePlan().id())
                .as("La asignación conserva la plantilla preventiva seleccionada")
                .isEqualTo(preventivePlan.id());
        assertThat(assignment.machine().id())
                .as("La asignación conserva la máquina seleccionada")
                .isEqualTo(machine.id());
        assertThat(assignment.intervalDays())
                .as("La asignación conserva el intervalo configurado")
                .isEqualTo(30);
        assertThat(assignment.noticeDays())
                .as("La asignación conserva el margen de aviso configurado")
                .isEqualTo(7);
        assertThat(assignment.nextDueDate())
                .as("La asignación conserva la próxima fecha prevista")
                .isEqualTo(NEXT_DUE_DATE);
        assertThat(assignment.active())
                .as("La asignación se crea activa")
                .isTrue();
    }

    /**
     * Comprueba que una plantilla inactiva no puede asignarse a una máquina, aunque esta se
     * encuentre activa.
     */
    @DisplayName("No se crea una asignación con una plantilla inactiva")
    @Test
    void rechazaCreacionCuandoLaPlantillaEstaInactiva() {

        // Creamos la máquina activa
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-ASSIGNMENT-INACTIVE-PLAN-001",
                        "Máquina de prueba activa",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-ASSIGNMENT-INACTIVE-PLAN-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos la plantilla preventiva y la desactivamos
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba inactivo",
                        "Instrucciones para probar una plantilla preventiva inactiva."
                )
        );
        preventivePlanService.updatePreventivePlan(
                preventivePlan.id(),
                new UpdatePreventivePlanRequest(
                        preventivePlan.name(),
                        preventivePlan.description(),
                        false
                )
        );

        // Comprobamos que una plantilla inactiva no puede asignarse
        assertThatThrownBy(() -> preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        preventivePlan.id(),
                        machine.id(),
                        30,
                        7,
                        NEXT_DUE_DATE
                )
        ))
                .as("Una plantilla inactiva no puede crear asignaciones preventivas")
                .isInstanceOf(PreventivePlanInactiveException.class)
                .hasMessage("El plan preventivo seleccionado no está activo.");

        // Comprobamos que el intento rechazado no crea la asignación
        assertThat(preventivePlanAssignmentRepository.existsByPreventivePlanIdAndMachineId(
                preventivePlan.id(),
                machine.id()
        ))
                .as("No se crea una asignación para la plantilla inactiva")
                .isFalse();
    }

    /**
     * Comprueba que una máquina que no está activa no puede recibir una asignación preventiva.
     * Se ejecuta para los dos estados no operativos publicados por la aplicación.
     *
     * @param status estado no operativo que se asigna a la máquina
     */
    @DisplayName("No se crea una asignación con una máquina no activa")
    @ParameterizedTest(name = "[{index}] La máquina {0} no puede recibir una asignación")
    @EnumSource(value = MachineStatus.class, names = {"INACTIVE", "RETIRED"})
    void rechazaCreacionCuandoLaMaquinaNoEstaActiva(MachineStatus status) {

        // Creamos la máquina y la cambiamos a un estado no operativo
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-ASSIGNMENT-" + status + "-MACHINE-001",
                        "Máquina de prueba no activa",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-ASSIGNMENT-" + status + "-MACHINE-001",
                        "Laboratorio",
                        TODAY
                )
        );
        machineService.updateMachine(
                machine.id(),
                new UpdateMachineRequest(
                        machine.name(),
                        machine.manufacturer(),
                        machine.model(),
                        machine.serialNumber(),
                        machine.location(),
                        status
                )
        );

        // Creamos la plantilla preventiva activa
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de máquina " + status,
                        "Instrucciones para probar una máquina no activa."
                )
        );

        // Comprobamos que la máquina no activa no puede recibir la asignación
        assertThatThrownBy(() -> preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        preventivePlan.id(),
                        machine.id(),
                        30,
                        7,
                        NEXT_DUE_DATE
                )
        ))
                .as("Una máquina " + status + " no puede recibir asignaciones preventivas")
                .isInstanceOf(MachineNotActiveException.class)
                .hasMessage("La máquina seleccionada no está activa.");

        // Comprobamos que el intento rechazado no crea la asignación
        assertThat(preventivePlanAssignmentRepository.existsByPreventivePlanIdAndMachineId(
                preventivePlan.id(),
                machine.id()
        ))
                .as("No se crea una asignación para la máquina no activa")
                .isFalse();
    }

    /**
     * Comprueba que una plantilla no puede asignarse dos veces a la misma máquina.
     */
    @DisplayName("No se duplica una asignación para la misma plantilla y máquina")
    @Test
    void rechazaCreacionCuandoYaExisteLaParejaPlantillaMaquina() {

        // Creamos la máquina activa
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-ASSIGNMENT-DUPLICATE-001",
                        "Máquina de prueba activa",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-ASSIGNMENT-DUPLICATE-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos la plantilla preventiva activa
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba duplicado",
                        "Instrucciones para probar una asignación preventiva duplicada."
                )
        );

        // Creamos la primera asignación válida
        preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        preventivePlan.id(),
                        machine.id(),
                        30,
                        7,
                        NEXT_DUE_DATE
                )
        );

        // Comprobamos que no se puede repetir la misma pareja plantilla-máquina
        assertThatThrownBy(() -> preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        preventivePlan.id(),
                        machine.id(),
                        30,
                        7,
                        NEXT_DUE_DATE
                )
        ))
                .as("Una plantilla no puede asignarse dos veces a la misma máquina")
                .isInstanceOf(PreventivePlanAssignmentAlreadyExistsException.class)
                .hasMessage("Ya existe una asignación para el plan preventivo y la máquina seleccionados.");

        // Comprobamos que se mantiene la primera asignación
        assertThat(preventivePlanAssignmentRepository.existsByPreventivePlanIdAndMachineId(
                preventivePlan.id(),
                machine.id()
        ))
                .as("La primera asignación permanece registrada")
                .isTrue();
    }
}
