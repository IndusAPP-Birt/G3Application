package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.AdminCreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.PreventivePlanAssignmentResponse;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import eus.birtlh.g3.preventive.repository.PreventivePlanAssignmentRepository;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PreventiveGenerationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2000, 1, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2000, 1, 5);

    @Autowired
    private PreventiveGenerationService preventiveGenerationService;

    @Autowired
    private MachineService machineService;

    @Autowired
    private PreventivePlanService preventivePlanService;

    @Autowired
    private PreventivePlanAssignmentService preventivePlanAssignmentService;

    @Autowired
    private PreventivePlanAssignmentRepository preventivePlanAssignmentRepository;

    @Autowired
    private PreventiveRepository preventiveRepository;

    @Test
    void generatesPendingPreventiveAndAdvancesAssignmentDueDate() {
        MachineResponse machine = machineService.createMachine(
                new AdminCreateMachineRequest(
                        "GEN-TEST-001",
                        "Máquina de prueba de generación",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "GEN-SN-001",
                        "Laboratorio",
                        TODAY
                )
        );

        PreventivePlanResponse plan = preventivePlanService.createPreventivePlan(
                new AdminCreatePreventivePlanRequest(
                        "Plan de prueba de generación",
                        "Plan utilizado por la prueba de generación automática."
                )
        );

        PreventivePlanAssignmentResponse assignment =
                preventivePlanAssignmentService.createPreventivePlanAssignment(
                        new AdminCreatePreventivePlanAssignmentRequest(
                                plan.id(),
                                machine.id(),
                                30,
                                7,
                                DUE_DATE
                        )
                );

        int generatedPreventives = preventiveGenerationService.generatePendingPreventives(TODAY);

        Page<Preventive> preventives = preventiveRepository.findByPreventivePlanAssignmentPreventivePlanId(
                plan.id(),
                PageRequest.of(0, 10)
        );
        PreventivePlanAssignment updatedAssignment = preventivePlanAssignmentRepository
                .findById(assignment.id())
                .orElseThrow();

        assertThat(generatedPreventives).isEqualTo(1);
        assertThat(preventives.getContent())
                .singleElement()
                .satisfies(preventive -> {
                    assertThat(preventive.getStatus()).isEqualTo(PreventiveStatus.PENDING);
                    assertThat(preventive.getDueDate()).isEqualTo(DUE_DATE);
                });
        assertThat(updatedAssignment.getNextDueDate()).isEqualTo(DUE_DATE.plusDays(30));
    }
}
