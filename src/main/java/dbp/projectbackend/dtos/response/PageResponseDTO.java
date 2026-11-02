package dbp.projectbackend.dtos.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Respuesta estándar de todos los listados paginados.
 * Se usa un DTO propio en vez de serializar {@link Page} directamente para que el JSON sea
 * estable (no depende de la implementación interna de Spring Data).
 *
 * @param content        elementos de la página actual
 * @param page           número de página (empieza en 0)
 * @param size           tamaño de página solicitado
 * @param totalElements  total de elementos que cumplen el filtro
 * @param totalPages     total de páginas
 * @param first          si es la primera página
 * @param last           si es la última página
 * @param sort           orden aplicado, por ejemplo {@code "fechaEmision: DESC"}
 */
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        String sort
) {

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                describe(page.getSort())
        );
    }

    private static String describe(Sort sort) {
        return sort.isSorted() ? sort.toString() : "UNSORTED";
    }
}
