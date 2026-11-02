package eus.birtlh.g3.common;

/**
 * Límites y valores predeterminados compartidos para la paginación de la API.
 */
public final class PaginationConstants {

    /** Índice mínimo de página admitido. */
    public static final long MIN_PAGE = 0;

    /** Tamaño mínimo de página admitido. */
    public static final long MIN_PAGE_SIZE = 1;

    /** Tamaño máximo de página admitido. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Valor predeterminado de página para parámetros HTTP. */
    public static final String DEFAULT_PAGE_VALUE = "0";

    /** Valor predeterminado de tamaño de página para parámetros HTTP. */
    public static final String DEFAULT_PAGE_SIZE_VALUE = "20";

    /** Impide la instanciación de esta clase de constantes. */
    private PaginationConstants() {
    }
}
