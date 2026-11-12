package eus.birtlh.g3.preventive;

import eus.birtlh.g3.preventive.repository.PreventivePlanAssignmentRepository;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio que genera ejecuciones preventivas a partir de las asignaciones activas de plantillas
 * a máquinas.
 */
@Service
@RequiredArgsConstructor
public class PreventiveGenerationService {

    private static final int BATCH_SIZE = 100;

    private final PreventivePlanAssignmentRepository preventivePlanAssignmentRepository;
    private final PreventiveRepository preventiveRepository;

    /**
     * Genera preventivos pendientes para las asignaciones activas que han entrado en su ventana
     * de aviso.
     *
     * <p>Tras generar un preventivo, avanza la próxima fecha prevista de la asignación según su
     * intervalo configurado.</p>
     *
     * @param today fecha de referencia para evaluar la planificación
     * @return número de preventivos generados
     */
    @Transactional
    public int generatePendingPreventives(LocalDate today) {
        int generatedPreventives = 0;
        int pageNumber = 0;
        Page<PreventivePlanAssignment> preventivePlanAssignments;

        do {
            preventivePlanAssignments = preventivePlanAssignmentRepository.findByActive(
                    true,
                    PageRequest.of(pageNumber, BATCH_SIZE, Sort.by("id").ascending())
            );

            for (PreventivePlanAssignment preventivePlanAssignment : preventivePlanAssignments) {
                LocalDate generationDate = today.plusDays(preventivePlanAssignment.getNoticeDays());

                if (!preventivePlanAssignment.getNextDueDate().isAfter(generationDate)) {
                    preventiveRepository.save(
                            new Preventive(
                                    preventivePlanAssignment,
                                    preventivePlanAssignment.getNextDueDate()
                            )
                    );

                    preventivePlanAssignment.advanceNextDueDate();
                    generatedPreventives++;
                }
            }

            pageNumber++;
        } while (preventivePlanAssignments.hasNext());

        return generatedPreventives;
    }
}
