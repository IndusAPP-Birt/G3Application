package eus.birtlh.g3.machine.dto;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Datos públicos de una máquina industrial persistida, expuestos por la API.
 *
 * <p>Incluye la información identificativa, técnica y de estado necesaria para consultar una
 * máquina, sin incorporar datos de sus futuros planes preventivos ni de sus ejecuciones.</p>
 *
 * @param id identificador único de la máquina
 * @param assetCode código patrimonial único e inmutable de la máquina
 * @param name nombre descriptivo de la máquina
 * @param manufacturer fabricante de la máquina; puede ser {@code null}
 * @param model modelo de la máquina; puede ser {@code null}
 * @param serialNumber número de serie de la máquina; puede ser {@code null}
 * @param location ubicación actual de la máquina; puede ser {@code null}
 * @param commissionedAt fecha de puesta en servicio de la máquina
 * @param status estado operativo actual de la máquina como opción de catálogo
 * @param createdAt fecha de creación del registro de la máquina
 * @param updatedAt fecha de última actualización del registro de la máquina
 */
public record MachineResponse(
        long id,
        String assetCode,
        String name,
        String manufacturer,
        String model,
        String serialNumber,
        String location,
        LocalDate commissionedAt,
        CatalogOptionResponse status,
        Instant createdAt,
        Instant updatedAt
) {
}
