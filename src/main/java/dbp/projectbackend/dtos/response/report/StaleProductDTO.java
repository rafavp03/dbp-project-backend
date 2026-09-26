package dbp.projectbackend.dtos.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StaleProductDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        LocalDate ultimaVenta,
        long diasSinVender,
        BigDecimal capitalInmovilizado
) {}
