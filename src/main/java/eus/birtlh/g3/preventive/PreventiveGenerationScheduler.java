package eus.birtlh.g3.preventive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Ejecuta periódicamente la generación de preventivos planificados.
 *
 * <p>Usa la zona horaria configurada para el cron también al calcular la fecha de referencia de
 * la planificación.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PreventiveGenerationScheduler {

    private final PreventiveGenerationService preventiveGenerationService;

    @Value("${preventive.generation.zone}")
    private String generationZone;

    /**
     * Genera los preventivos que han entrado en su ventana de aviso según la fecha de la zona
     * configurada.
     */
    @Scheduled(
            cron = "${preventive.generation.cron}",
            zone = "${preventive.generation.zone}"
    )
    public void generatePendingPreventives() {
        int generatedPreventives =
                preventiveGenerationService.generatePendingPreventives(
                        LocalDate.now(ZoneId.of(generationZone))
                );

        log.info("Se han generado {} preventivos.", generatedPreventives);
    }
}
