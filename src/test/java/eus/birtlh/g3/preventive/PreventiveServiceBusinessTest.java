package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.execution.AssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.execution.PreventiveResponse;
import eus.birtlh.g3.preventive.dto.execution.RecordPreventiveWorkRequest;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.exceptions.execution.InvalidPreventiveDateRangeException;
import eus.birtlh.g3.preventive.exceptions.execution.InvalidPreventiveStatusTransitionException;
import eus.birtlh.g3.preventive.exceptions.execution.UserIsNotInspectorException;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserService;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PreventiveServiceBusinessTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);

    @Autowired private PreventiveService preventiveService;
    @Autowired private PreventiveGenerationService preventiveGenerationService;
    @Autowired private PreventivePlanAssignmentService preventivePlanAssignmentService;
    @Autowired private PreventivePlanService preventivePlanService;
    @Autowired private MachineService machineService;
    @Autowired private UserService userService;
    @Autowired private PreventiveRepository preventiveRepository;

    private UserResponse administrator;

    /**
     * Prepara el administrador que debe quedar registrado al asignar los preventivos de cada
     * prueba.
     */
    @BeforeEach
    void createAdministrator() {
        administrator = createUser("administrator.business@test.local", Role.ROLE_ADMINISTRATOR);
    }

    /** Comprueba que solo un usuario con rol inspector puede recibir un preventivo pendiente. */
    @DisplayName("Rechaza asignar un preventivo pendiente a un usuario que no es inspector")
    @Test
    void rechazaAsignacionAUsuarioQueNoEsInspector() {
        Preventive preventive = createPendingPreventive("NON-INSPECTOR");
        UserResponse technician = createUser("technician@test.local", Role.ROLE_MAINTENANCE_TECHNICIAN);

        assertThatThrownBy(() -> preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(technician.id()),
                administrator.email()
        ))
                .as("Un técnico no puede recibir un preventivo")
                .isInstanceOf(UserIsNotInspectorException.class);
        assertThat(preventiveRepository.findById(preventive.getId()).orElseThrow().getStatus())
                .as("El preventivo se mantiene pendiente")
                .isEqualTo(PreventiveStatus.PENDING);
    }

    /** Comprueba que un preventivo ya asignado no puede recibir un segundo inspector. */
    @DisplayName("Rechaza reasignar un preventivo que ya está ASSIGNED")
    @Test
    void rechazaReasignacionDePreventivoYaAsignado() {
        Preventive preventive = createPendingPreventive("REASSIGNMENT");
        UserResponse assignedInspector = createUser("assigned@test.local", Role.ROLE_INSPECTOR);
        UserResponse otherInspector = createUser("other@test.local", Role.ROLE_INSPECTOR);

        // El administrador realiza la primera asignación del preventivo
        preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(assignedInspector.id()),
                administrator.email()
        );

        assertThatThrownBy(() -> preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(otherInspector.id()),
                administrator.email()
        ))
                .as("No se sustituye el inspector de un preventivo ya asignado")
                .isInstanceOf(InvalidPreventiveStatusTransitionException.class);
        assertThat(preventiveRepository.findById(preventive.getId()).orElseThrow().getAssignedInspector().getId())
                .as("Se conserva el inspector original")
                .isEqualTo(assignedInspector.id());
    }

    /** Comprueba que el inspector asignado puede corregir su informe durante la ejecución. */
    @DisplayName("Permite corregir el trabajo mientras el preventivo está en progreso")
    @Test
    void permiteCorregirTrabajoEnPreventivoEnProgreso() {
        Preventive preventive = createPendingPreventive("WORK-CORRECTION");
        UserResponse inspector = createUser("work@test.local", Role.ROLE_INSPECTOR);

        // El administrador asigna el preventivo antes de que el inspector lo ejecute
        preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );
        preventiveService.startPreventive(preventive.getId(), inspector.email());
        preventiveService.recordWorkPerformed(preventive.getId(), new RecordPreventiveWorkRequest("Trabajo inicial"), inspector.email());

        PreventiveResponse corrected = preventiveService.recordWorkPerformed(
                preventive.getId(), new RecordPreventiveWorkRequest("Trabajo corregido"), inspector.email()
        );

        assertThat(corrected.status()).as("El preventivo continúa en progreso")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(corrected.workPerformed()).as("Se conserva el último informe registrado")
                .isEqualTo("Trabajo corregido");
    }

    /** Comprueba que cada inspector consulta únicamente los preventivos que tiene asignados. */
    @DisplayName("La bandeja del inspector no muestra preventivos de otros inspectores")
    @Test
    void devuelveSoloLosPreventivosDelInspectorAutenticado() {
        Preventive first = createPendingPreventive("INBOX-A");
        Preventive second = createPendingPreventive("INBOX-B");
        UserResponse inspectorA = createUser("inbox-a@test.local", Role.ROLE_INSPECTOR);
        UserResponse inspectorB = createUser("inbox-b@test.local", Role.ROLE_INSPECTOR);

        // El administrador asigna un preventivo a cada inspector
        preventiveService.assignInspectorToPreventive(
                first.getId(),
                new AssignInspectorToPreventiveRequest(inspectorA.id()),
                administrator.email()
        );
        preventiveService.assignInspectorToPreventive(
                second.getId(),
                new AssignInspectorToPreventiveRequest(inspectorB.id()),
                administrator.email()
        );

        PageResponse<PreventiveResponse> inbox = preventiveService
                .getPreventivesByAuthenticatedAssignedInspector(0, 20, inspectorA.email());

        assertThat(inbox.content()).as("La bandeja contiene un único preventivo")
                .hasSize(1);
        assertThat(inbox.content().getFirst().id()).as("El preventivo pertenece al inspector autenticado")
                .isEqualTo(first.getId());
    }

    /** Comprueba que el filtro por fecha rechaza un rango cuyo inicio es posterior al final. */
    @DisplayName("Rechaza un rango de fechas preventivas invertido")
    @Test
    void rechazaRangoDeFechasInvalido() {
        assertThatThrownBy(() -> preventiveService.getPreventivesByDueDate(
                0, 20, TODAY.plusDays(1), TODAY
        ))
                .as("La fecha inicial no puede ser posterior a la final")
                .isInstanceOf(InvalidPreventiveDateRangeException.class);
    }

    private Preventive createPendingPreventive(String suffix) {
        MachineResponse machine = machineService.createMachine(new CreateMachineRequest(
                "TEST-BUSINESS-" + suffix, "Máquina " + suffix, "Fabricante", "Modelo",
                "SN-BUSINESS-" + suffix, "Laboratorio", TODAY
        ));
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest("Plan " + suffix, "Descripción " + suffix)
        );
        preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(plan.id(), machine.id(), 30, 7, TODAY.plusDays(5))
        );
        preventiveGenerationService.generatePendingPreventives(TODAY);
        return preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                plan.id(), PageRequest.of(0, 1)
        ).getContent().getFirst();
    }

    private UserResponse createUser(String email, Role role) {
        return userService.createUser(new CreateUserRequest(
                "Usuario", "Prueba", email, "Password123!", role
        ));
    }
}
