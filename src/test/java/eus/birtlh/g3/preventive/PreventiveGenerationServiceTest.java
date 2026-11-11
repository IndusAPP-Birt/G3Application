package eus.birtlh.g3.preventive;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.AdminCreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
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
    private PreventivePlanRepository preventivePlanRepository;

    @Autowired
    private PreventiveRepository preventiveRepository;

    @Test
    void generatesPendingPreventiveAndAdvancesPlanDueDate() {
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
                        machine.id(),
                        "Plan de prueba de generación",
                        "Plan utilizado por la prueba de generación automática.",
                        30,
                        7,
                        DUE_DATE
                )
        );

        int generatedPreventives = preventiveGenerationService.generatePendingPreventives(TODAY);

        Page<Preventive> preventives = preventiveRepository.findByPreventivePlanId(
                plan.id(),
                PageRequest.of(0, 10)
        );
        PreventivePlan updatedPlan = preventivePlanRepository.findById(plan.id()).orElseThrow();

        assertThat(generatedPreventives).isEqualTo(1);
        assertThat(preventives.getContent())
                .singleElement()
                .satisfies(preventive -> {
                    assertThat(preventive.getStatus()).isEqualTo(PreventiveStatus.PENDING);
                    assertThat(preventive.getDueDate()).isEqualTo(DUE_DATE);
                });
        assertThat(updatedPlan.getNextDueDate()).isEqualTo(DUE_DATE.plusDays(30));
    }
}
