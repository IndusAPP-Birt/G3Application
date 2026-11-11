package eus.birtlh.g3.preventive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Ejecuta periódicamente la generación de preventivos planificados.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PreventiveGenerationScheduler {

    private final PreventiveGenerationService preventiveGenerationService;

    /**
     * Genera los preventivos que han entrado en su ventana de aviso.
     */
    @Scheduled(
            cron = "${preventive.generation.cron}",
            zone = "${preventive.generation.zone}"
    )
    public void generatePendingPreventives() {
        int generatedPreventives =
                preventiveGenerationService.generatePendingPreventives(LocalDate.now());

        log.info("Se han generado {} preventivos.", generatedPreventives);
    }
}
