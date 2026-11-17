package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.assignment.PreventivePlanAssignmentResponse;
import eus.birtlh.g3.preventive.dto.assignment.UpdatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.dto.plan.UpdatePreventivePlanRequest;
import eus.birtlh.g3.preventive.repository.PreventivePlanAssignmentRepository;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PreventiveGenerationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);

    @Autowired
    private MachineService machineService;

    @Autowired
    private PreventivePlanService preventivePlanService;

    @Autowired
    private PreventivePlanAssignmentService preventivePlanAssignmentService;

    @Autowired
    private PreventiveGenerationService preventiveGenerationService;

    @Autowired
    private PreventivePlanAssignmentRepository preventivePlanAssignmentRepository;

    @Autowired
    private PreventiveRepository preventiveRepository;

    /**
     * Comprueba que una asignación activa genera un preventivo al entrar en su ventana de aviso y
     * avanza después su próxima fecha prevista.
     */
    @DisplayName("Genera un preventivo dentro de la ventana de aviso")
    @Test
    void generaPreventivoYAvanzaLaPlanificacionDentroDeLaVentanaDeAviso() {
        PreventivePlanAssignmentResponse assignment = createAssignment("WINDOW", TODAY.plusDays(5));

        int generated = preventiveGenerationService.generatePendingPreventives(TODAY);
        Preventive preventive = findGeneratedPreventive(assignment.preventivePlan().id());
        PreventivePlanAssignment updatedAssignment = preventivePlanAssignmentRepository
                .findById(assignment.id())
                .orElseThrow();

        assertThat(generated).as("Se genera un único preventivo").isEqualTo(1);
        assertThat(preventive.getStatus()).as("El preventivo se crea pendiente")
                .isEqualTo(PreventiveStatus.PENDING);
        assertThat(preventive.getDueDate()).as("Conserva la fecha prevista de la asignación")
                .isEqualTo(TODAY.plusDays(5));
        assertThat(updatedAssignment.getNextDueDate()).as("La planificación avanza un intervalo")
                .isEqualTo(TODAY.plusDays(35));
    }

    /**
     * Comprueba que una asignación fuera de la ventana de aviso no genera preventivos ni avanza
     * su calendario.
     */
    @DisplayName("No genera preventivos fuera de la ventana de aviso")
    @Test
    void noGeneraPreventivoFueraDeLaVentanaDeAviso() {
        PreventivePlanAssignmentResponse assignment = createAssignment("FUTURE", TODAY.plusDays(8));

        int generated = preventiveGenerationService.generatePendingPreventives(TODAY);
        PreventivePlanAssignment unchangedAssignment = preventivePlanAssignmentRepository
                .findById(assignment.id())
                .orElseThrow();

        assertThat(generated).as("No se genera ningún preventivo").isZero();
        assertThat(preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                assignment.preventivePlan().id(), PageRequest.of(0, 1)).getContent())
                .as("No existe una ejecución para la plantilla")
                .isEmpty();
        assertThat(unchangedAssignment.getNextDueDate()).as("La fecha prevista no se modifica")
                .isEqualTo(TODAY.plusDays(8));
    }

    /**
     * Comprueba que una asignación desactivada no genera preventivos aunque su fecha esté dentro
     * de la ventana de aviso.
     */
    @DisplayName("No genera preventivos desde una asignación inactiva")
    @Test
    void noGeneraPreventivoDesdeUnaAsignacionInactiva() {
        PreventivePlanAssignmentResponse assignment = createAssignment("INACTIVE", TODAY.plusDays(5));
        preventivePlanAssignmentService.updatePreventivePlanAssignment(
                assignment.id(),
                new UpdatePreventivePlanAssignmentRequest(30, 7, TODAY.plusDays(5), false)
        );

        int generated = preventiveGenerationService.generatePendingPreventives(TODAY);
        PreventivePlanAssignment unchangedAssignment = preventivePlanAssignmentRepository
                .findById(assignment.id())
                .orElseThrow();

        assertThat(generated).as("La asignación inactiva no genera preventivos").isZero();
        assertThat(unchangedAssignment.isActive()).as("La asignación permanece inactiva").isFalse();
        assertThat(unchangedAssignment.getNextDueDate()).as("Su planificación no avanza")
                .isEqualTo(TODAY.plusDays(5));
    }

    /**
     * Comprueba que una asignación vuelve a ser elegible para generar preventivos después de
     * reactivarse.
     */
    @DisplayName("Una asignación reactivada vuelve a generar preventivos")
    @Test
    void generaPreventivoDespuesDeReactivarLaAsignacion() {
        PreventivePlanAssignmentResponse assignment = createAssignment("REACTIVATED", TODAY.plusDays(5));
        preventivePlanAssignmentService.updatePreventivePlanAssignment(
                assignment.id(),
                new UpdatePreventivePlanAssignmentRequest(30, 7, TODAY.plusDays(5), false)
        );
        preventiveGenerationService.generatePendingPreventives(TODAY);

        preventivePlanAssignmentService.updatePreventivePlanAssignment(
                assignment.id(),
                new UpdatePreventivePlanAssignmentRequest(30, 7, TODAY.plusDays(5), true)
        );
        int generated = preventiveGenerationService.generatePendingPreventives(TODAY);

        assertThat(generated).as("La asignación reactivada vuelve a ser elegible")
                .isEqualTo(1);
        assertThat(preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                assignment.preventivePlan().id(), PageRequest.of(0, 1)).getContent())
                .as("La reactivación genera el preventivo pendiente")
                .hasSize(1);
    }

    /**
     * Comprueba que un preventivo conserva el nombre y la descripción de la plantilla que tenía
     * en el momento de generarse.
     */
    @DisplayName("El preventivo conserva una instantánea de la plantilla")
    @Test
    void conservaInstantaneaDeLaPlantillaDespuesDeSuActualizacion() {
        PreventivePlanAssignmentResponse assignment = createAssignment("SNAPSHOT", TODAY.plusDays(5));
        String originalName = assignment.preventivePlan().name();
        String originalDescription = assignment.preventivePlan().description();

        preventiveGenerationService.generatePendingPreventives(TODAY);
        preventivePlanService.updatePreventivePlan(
                assignment.preventivePlan().id(),
                new UpdatePreventivePlanRequest("Plantilla modificada", "Descripción modificada", true)
        );
        Preventive preventive = findGeneratedPreventive(assignment.preventivePlan().id());

        assertThat(preventive.getPlanName()).as("Conserva el nombre original de la plantilla")
                .isEqualTo(originalName);
        assertThat(preventive.getPlanDescription()).as("Conserva la descripción original")
                .isEqualTo(originalDescription);
    }

    private PreventivePlanAssignmentResponse createAssignment(String suffix, LocalDate nextDueDate) {
        MachineResponse machine = machineService.createMachine(new CreateMachineRequest(
                "TEST-GENERATION-" + suffix,
                "Máquina de generación " + suffix,
                "Fabricante", "Modelo", "SN-GENERATION-" + suffix, "Laboratorio", TODAY
        ));
        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de generación " + suffix,
                        "Descripción original " + suffix
                )
        );
        return preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(plan.id(), machine.id(), 30, 7, nextDueDate)
        );
    }

    private Preventive findGeneratedPreventive(long preventivePlanId) {
        return preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                preventivePlanId,
                PageRequest.of(0, 1)
        ).getContent().getFirst();
    }
}
