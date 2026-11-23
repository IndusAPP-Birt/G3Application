package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.execution.AssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.execution.PreventiveResponse;
import eus.birtlh.g3.preventive.dto.execution.RecordPreventiveWorkRequest;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.exceptions.assignment.InspectorNotAssignedToPreventiveException;
import eus.birtlh.g3.preventive.exceptions.execution.InvalidPreventiveStatusTransitionException;
import eus.birtlh.g3.preventive.exceptions.execution.PreventiveWorkReportRequiredException;
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

    private UserResponse administrator;

    /**
     * Prepara el administrador que debe quedar registrado al asignar los preventivos de cada
     * prueba.
     */
    @BeforeEach
    void createAdministrator() {
        administrator = userService.createUser(
                new CreateUserRequest(
                        "Administrador",
                        "Prueba",
                        "administrator.preventive@test.local",
                        "Password123!",
                        Role.ROLE_ADMINISTRATOR
                )
        );
    }

    /**
     * Comprueba que el inspector asignado puede iniciar un preventivo. La máquina y la plantilla
     * existen por separado y se vinculan mediante una asignación preventiva. Cuando su
     * planificación entra en la ventana de aviso, el proceso automático genera un preventivo
     * pendiente. Tras asignarlo al inspector, pasa a {@link PreventiveStatus#ASSIGNED}; cuando
     * ese mismo inspector lo inicia, pasa a {@link PreventiveStatus#IN_PROGRESS} y registra su
     * fecha de inicio.
     */
    @DisplayName("El inspector asignado puede iniciar un preventivo ASSIGNED")
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
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

        // El administrador asigna el preventivo al inspector
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );

        // El inspector inicia el preventivo
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // Comprobamos que el preventivo pasa a IN_PROGRESS
        assertThat(started.status())
                .as("El preventivo debe pasar a IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(started.startedAt())
                .as("'startedAt()' muestra la fecha en la que el preventivo pasa a IN_PROGRESS")
                .isNotNull();
        // Comprobamos que conserva al inspector asignado
        assertThat(started.assignedInspector().id())
                .as("El inspector asignado es el mismo cuando el preventivo pasa a IN_PROGRESS")
                .isEqualTo(inspector.id());
        // Comprobamos que conserva el administrador que realizó la asignación
        assertThat(started.assignedByAdministrator().id())
                .as("El administrador asignador se conserva durante la ejecución")
                .isEqualTo(administrator.id());
    }

    /**
     * Comprueba que un inspector no asignado no puede iniciar un preventivo. Tras rechazar la
     * operación, el preventivo conserva el estado {@link PreventiveStatus#ASSIGNED} y no registra
     * fecha de inicio.
     */
    @DisplayName("Un inspector no asignado no puede iniciar el preventivo")
    @Test
    void rechazaInicioCuandoElInspectorNoEstaAsignadoAlPreventivo() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-REJECT-START-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-REJECT-START-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de rechazo",
                        "Instrucciones para probar la propiedad del inspector."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Asignado",
                        "inspector.assigned@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // Creamos a otro inspector sin asignación sobre el preventivo
        UserResponse otherInspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Ajeno",
                        "inspector.other@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo al primer inspector
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );

        // Comprobamos que el inspector ajeno no puede iniciar el preventivo
        assertThatThrownBy(() -> preventiveService.startPreventive(
                assigned.id(),
                otherInspector.email()
        ))
                .as("Un inspector ajeno no puede iniciar el preventivo")
                .isInstanceOf(InspectorNotAssignedToPreventiveException.class)
                .hasMessage("El preventivo no está asignado al inspector autenticado.");

        Preventive unchangedPreventive = preventiveRepository.findById(assigned.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como ASSIGNED")
                .isEqualTo(PreventiveStatus.ASSIGNED);
        assertThat(unchangedPreventive.getStartedAt())
                .as("'startedAt' se mantiene vacía")
                .isNull();
    }

    /**
     * Comprueba que un inspector no asignado no puede registrar trabajo en un preventivo en curso.
     * Tras rechazar la operación, el preventivo conserva el estado
     * {@link PreventiveStatus#IN_PROGRESS} y no registra trabajo realizado.
     */
    @DisplayName("Un inspector no asignado no puede registrar trabajo en el preventivo")
    @Test
    void rechazaRegistroDeTrabajoCuandoElInspectorNoEstaAsignadoAlPreventivo() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-REJECT-WORK-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-REJECT-WORK-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de rechazo de trabajo",
                        "Instrucciones para probar que un inspector ajeno no puede registrar trabajo."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Asignado",
                        "inspector.assigned@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo al inspector
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );

        // El inspector asignado inicia el preventivo
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // Comprobamos que el preventivo está en curso antes del intento rechazado
        assertThat(started.status())
                .as("El preventivo debe pasar a IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);

        // Creamos a otro inspector sin asignación sobre el preventivo
        UserResponse otherInspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Ajeno",
                        "inspector.other@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // Comprobamos que el inspector ajeno no puede registrar trabajo
        assertThatThrownBy(() -> preventiveService.recordWorkPerformed(
                started.id(),
                new RecordPreventiveWorkRequest(
                        "Registro de trabajo por el inspector B"
                ),
                otherInspector.email()
        ))
                .as("Un inspector ajeno no puede registrar trabajo en el preventivo")
                .isInstanceOf(InspectorNotAssignedToPreventiveException.class)
                .hasMessage("El preventivo no está asignado al inspector autenticado.");

        Preventive unchangedPreventive = preventiveRepository.findById(started.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(unchangedPreventive.getWorkPerformed())
                .as("El trabajo realizado se mantiene vacío")
                .isNull();
    }

    /**
     * Comprueba que un inspector no asignado no puede completar un preventivo en curso, aunque
     * el inspector asignado haya registrado trabajo. Tras rechazar la operación, el preventivo
     * conserva su estado, el trabajo registrado y no establece fecha de finalización.
     */
    @DisplayName("Un inspector no asignado no puede completar el preventivo")
    @Test
    void rechazaFinalizacionCuandoElInspectorNoEstaAsignadoAlPreventivo() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-REJECT-COMPLETION-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-REJECT-COMPLETION-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de rechazo de finalización",
                        "Instrucciones para probar que un inspector ajeno no puede completar el preventivo."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Asignado",
                        "inspector.assigned@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo al inspector
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );

        // El inspector asignado inicia el preventivo
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // El inspector asignado registra el trabajo realizado
        String workPerformed = "Revisión y ajuste realizados por el inspector asignado";
        preventiveService.recordWorkPerformed(
                started.id(),
                new RecordPreventiveWorkRequest(workPerformed),
                inspector.email()
        );

        // Creamos a otro inspector sin asignación sobre el preventivo
        UserResponse otherInspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Ajeno",
                        "inspector.other@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // Comprobamos que el inspector ajeno no puede completar el preventivo
        assertThatThrownBy(() -> preventiveService.completePreventive(
                started.id(),
                otherInspector.email()
        ))
                .as("Un inspector ajeno no puede completar el preventivo")
                .isInstanceOf(InspectorNotAssignedToPreventiveException.class)
                .hasMessage("El preventivo no está asignado al inspector autenticado.");

        Preventive unchangedPreventive = preventiveRepository.findById(started.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(unchangedPreventive.getWorkPerformed())
                .as("El trabajo realizado se conserva")
                .isEqualTo(workPerformed);
        assertThat(unchangedPreventive.getCompletedAt())
                .as("'completedAt' se mantiene vacía")
                .isNull();
    }

    /**
     * Comprueba que un preventivo pendiente no puede iniciarse porque aún no tiene un inspector
     * asignado. La validación de propiedad se realiza antes de comprobar la transición de estado.
     */
    @DisplayName("No se puede iniciar un preventivo PENDING")
    @Test
    void rechazaInicioDesdePendingCuandoNoHayInspectorAsignado() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-PENDING-START-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-PENDING-START-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de inicio pendiente",
                        "Instrucciones para probar el inicio desde un preventivo pendiente."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos un inspector que no está asignado al preventivo
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Pendiente",
                        "inspector.pending@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // Comprobamos que no puede iniciar un preventivo sin asignación
        assertThatThrownBy(() -> preventiveService.startPreventive(
                preventive.getId(),
                inspector.email()
        ))
                .as("Un preventivo PENDING no puede iniciarse sin inspector asignado")
                .isInstanceOf(InspectorNotAssignedToPreventiveException.class)
                .hasMessage("El preventivo no está asignado al inspector autenticado.");

        Preventive unchangedPreventive = preventiveRepository.findById(preventive.getId())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como PENDING")
                .isEqualTo(PreventiveStatus.PENDING);
        assertThat(unchangedPreventive.getStartedAt())
                .as("'startedAt' se mantiene vacía")
                .isNull();
    }

    /**
     * Comprueba que un preventivo ya completado no puede volver a iniciarse, incluso cuando la
     * solicitud la realiza el inspector que lo tiene asignado.
     */
    @DisplayName("No se puede iniciar un preventivo COMPLETED")
    @Test
    void rechazaInicioDesdeCompletedAunqueLoSoliciteElInspectorAsignado() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-COMPLETED-START-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-COMPLETED-START-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de inicio completado",
                        "Instrucciones para probar el inicio desde un preventivo completado."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Completado",
                        "inspector.completed@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo y el inspector lo inicia
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // Registramos el trabajo necesario para completar el preventivo
        preventiveService.recordWorkPerformed(
                started.id(),
                new RecordPreventiveWorkRequest("Revisión finalizada para probar una transición inválida."),
                inspector.email()
        );

        // Completamos el preventivo con el inspector asignado
        PreventiveResponse completed = preventiveService.completePreventive(
                started.id(),
                inspector.email()
        );

        // Comprobamos que no puede volver a iniciar un preventivo completado
        assertThatThrownBy(() -> preventiveService.startPreventive(
                completed.id(),
                inspector.email()
        ))
                .as("Un preventivo COMPLETED no puede volver a iniciarse")
                .isInstanceOf(InvalidPreventiveStatusTransitionException.class)
                .hasMessage("Solo se puede iniciar un preventivo asignado.");

        Preventive unchangedPreventive = preventiveRepository.findById(completed.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como COMPLETED")
                .isEqualTo(PreventiveStatus.COMPLETED);
        assertThat(unchangedPreventive.getStartedAt())
                .as("'startedAt' conserva la fecha de inicio original")
                .isNotNull();
        assertThat(unchangedPreventive.getCompletedAt())
                .as("'completedAt' conserva la fecha de finalización")
                .isNotNull();
    }

    /**
     * Comprueba que el inspector asignado no puede registrar trabajo mientras el preventivo está
     * asignado, ya que esa operación solo se permite durante el estado
     * {@link PreventiveStatus#IN_PROGRESS}.
     */
    @DisplayName("No se puede registrar trabajo en un preventivo ASSIGNED")
    @Test
    void rechazaRegistroDeTrabajoCuandoElPreventivoNoEstaEnProgreso() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-ASSIGNED-WORK-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-ASSIGNED-WORK-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de trabajo asignado",
                        "Instrucciones para probar el registro de trabajo fuera de progreso."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Trabajo asignado",
                        "inspector.assigned-work@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo sin iniciarlo
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );

        // Comprobamos que no se puede registrar trabajo antes de iniciar el preventivo
        assertThatThrownBy(() -> preventiveService.recordWorkPerformed(
                assigned.id(),
                new RecordPreventiveWorkRequest("Intento de registrar trabajo antes del inicio."),
                inspector.email()
        ))
                .as("Solo se puede registrar trabajo en un preventivo IN_PROGRESS")
                .isInstanceOf(InvalidPreventiveStatusTransitionException.class)
                .hasMessage("Solo se puede guardar un trabajo en un preventivo en progreso.");

        Preventive unchangedPreventive = preventiveRepository.findById(assigned.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como ASSIGNED")
                .isEqualTo(PreventiveStatus.ASSIGNED);
        assertThat(unchangedPreventive.getWorkPerformed())
                .as("El trabajo realizado se mantiene vacío")
                .isNull();
    }

    /**
     * Comprueba que el inspector asignado no puede completar un preventivo en curso si todavía no
     * ha registrado el trabajo realizado.
     */
    @DisplayName("No se puede completar un preventivo sin trabajo registrado")
    @Test
    void rechazaFinalizacionCuandoNoSeHaRegistradoTrabajo() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-WORK-REQUIRED-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-WORK-REQUIRED-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de trabajo obligatorio",
                        "Instrucciones para probar la finalización sin trabajo registrado."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Trabajo obligatorio",
                        "inspector.work-required@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo y el inspector lo inicia
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // Comprobamos que no se puede completar sin registrar el trabajo realizado
        assertThatThrownBy(() -> preventiveService.completePreventive(
                started.id(),
                inspector.email()
        ))
                .as("Un preventivo requiere trabajo registrado antes de completarse")
                .isInstanceOf(PreventiveWorkReportRequiredException.class)
                .hasMessage("Debe registrar el trabajo realizado antes de completar el preventivo.");

        Preventive unchangedPreventive = preventiveRepository.findById(started.id())
                .orElseThrow();

        // Comprobamos que el intento rechazado no modifica el preventivo
        assertThat(unchangedPreventive.getStatus())
                .as("El preventivo se mantiene como IN_PROGRESS")
                .isEqualTo(PreventiveStatus.IN_PROGRESS);
        assertThat(unchangedPreventive.getWorkPerformed())
                .as("El trabajo realizado se mantiene vacío")
                .isNull();
        assertThat(unchangedPreventive.getCompletedAt())
                .as("'completedAt' se mantiene vacía")
                .isNull();
    }

    /**
     * Comprueba que el inspector asignado puede completar un preventivo en curso cuando ha
     * registrado el trabajo realizado. El cierre cambia su estado a
     * {@link PreventiveStatus#COMPLETED} y registra la fecha de finalización.
     */
    @DisplayName("El inspector asignado puede completar un preventivo con trabajo registrado")
    @Test
    void completaPreventivoEnProgresoConTrabajoRegistradoCuandoLoSolicitaElInspectorAsignado() {

        // Creamos la máquina
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-COMPLETION-001",
                        "Máquina de prueba",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-COMPLETION-001",
                        "Laboratorio",
                        TODAY
                )
        );

        // Creamos el plan preventivo
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de finalización",
                        "Instrucciones para probar la finalización de un preventivo."
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

        // Generamos el preventivo pendiente a partir de la asignación configurada
        preventiveGenerationService.generatePendingPreventives(TODAY);

        // Recuperamos el preventivo generado según el plan
        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        plan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        // Creamos al inspector que recibirá la asignación
        UserResponse inspector = userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        "Finalización",
                        "inspector.completion@test.local",
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );

        // El administrador asigna el preventivo y el inspector lo inicia
        PreventiveResponse assigned = preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id()),
                administrator.email()
        );
        PreventiveResponse started = preventiveService.startPreventive(
                assigned.id(),
                inspector.email()
        );

        // El inspector registra el trabajo realizado antes de completar el preventivo
        String workPerformed = "Revisión finalizada y equipo validado";
        preventiveService.recordWorkPerformed(
                started.id(),
                new RecordPreventiveWorkRequest(workPerformed),
                inspector.email()
        );

        // El inspector asignado completa el preventivo
        PreventiveResponse completed = preventiveService.completePreventive(
                started.id(),
                inspector.email()
        );

        // Comprobamos que el cierre actualiza el estado y conserva el trabajo registrado
        assertThat(completed.status())
                .as("El preventivo debe pasar a COMPLETED")
                .isEqualTo(PreventiveStatus.COMPLETED);
        assertThat(completed.workPerformed())
                .as("El trabajo realizado se conserva al completar el preventivo")
                .isEqualTo(workPerformed);
        assertThat(completed.completedAt())
                .as("'completedAt' registra la fecha de finalización")
                .isNotNull();
    }
}
