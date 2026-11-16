package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.execution.AssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.execution.PreventiveResponse;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserService;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PreventiveServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
    private static final LocalDate DUE_DATE = TODAY.plusDays(5);

    @Autowired
    MachineService machineService;

    @Autowired
    PreventivePlanService preventivePlanService;

    @Autowired
    PreventivePlanAssignmentService preventivePlanAssignmentService;

    @Autowired
    PreventiveGenerationService preventiveGenerationService;

    @Autowired
    PreventiveRepository preventiveRepository;

    @Autowired
    PreventiveService preventiveService;

    @Autowired
    UserService userService;

    @DisplayName("El inspector asignado puede iniciar un preventivo ASSIGNED")
    /**
     * Comprueba que el inspector asignado puede iniciar un preventivo. La máquina y la plantilla
     * existen por separado y se vinculan mediante una asignación preventiva. Cuando su
     * planificación entra en la ventana de aviso, el proceso automático genera un preventivo
     * pendiente. Tras asignarlo al inspector, pasa a {@link PreventiveStatus#ASSIGNED}; cuando
     * ese mismo inspector lo inicia, pasa a {@link PreventiveStatus#IN_PROGRESS} y registra su
     * fecha de inicio.
     */
    @Test
    void iniciaUnPreventivoAsignadoCuandoLoSolicitaElInspectorAsignado() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-START-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-START-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de inicio",
                        "Instrucciones para probar el inicio de un preventivo"
                )
        );

        // Creamos la asignación
        preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        plan.id(),
                        machine.id(),
                        30,
                        7,
                        DUE_DATE
                )
        );

        // Avanza a la próxima fecha prevista de la asignación según el intervalo configurado
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos un inspector
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Prueba",
                        "inspector.start@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // Asignamos el preventivo al inspector
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id())
        );

        // El inspector inicia el preventivo
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // Validaciones
        assertThat(started.status())
                .as("El preventivo debe pasar a IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(started.startedAt())
                .as("'startedAt()' muestra la fecha en la que el preventivo pasa a IN_PROGRESS")
                .isNotNull();
        assertThat(started.assignedInspector().id())
                .as("El inspector asignado es el mismo cuando el preventivo pasa a IN_PROGRESS")
                .isEqualTo(inspector.id());
    }
}
