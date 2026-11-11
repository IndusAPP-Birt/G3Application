package eus.birtlh.g3.preventive;

import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio que genera ejecuciones preventivas a partir de los planes activos.
 */
@Service
@RequiredArgsConstructor
public class PreventiveGenerationService {

    private static final int BATCH_SIZE = 100;

    private final PreventivePlanRepository preventivePlanRepository;
    private final PreventiveRepository preventiveRepository;

    /**
     * Genera preventivos pendientes para los planes activos que han entrado en su ventana de
     * aviso.
     *
     * <p>Tras generar un preventivo, avanza la próxima fecha prevista del plan según su
     * intervalo configurado.</p>
     *
     * @param today fecha de referencia para evaluar la planificación
     * @return número de preventivos generados
     */
    @Transactional
    public int generatePendingPreventives(LocalDate today) {
        int generatedPreventives = 0;
        int pageNumber = 0;
        Page<PreventivePlan> preventivePlans;

        do {
            preventivePlans = preventivePlanRepository.findByActive(
                    true,
                    PageRequest.of(pageNumber, BATCH_SIZE, Sort.by("id").ascending())
            );

            for (PreventivePlan preventivePlan : preventivePlans) {
                LocalDate generationDate = today.plusDays(preventivePlan.getNoticeDays());

                if (!preventivePlan.getNextDueDate().isAfter(generationDate)) {
                    preventiveRepository.save(
                            new Preventive(preventivePlan, preventivePlan.getNextDueDate())
                    );

                    preventivePlan.advanceNextDueDate();
                    generatedPreventives++;
                }
            }

            pageNumber++;
        } while (preventivePlans.hasNext());

        return generatedPreventives;
    }
}
