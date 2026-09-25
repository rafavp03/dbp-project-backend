package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoParadoDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        LocalDate ultimaVenta,
        long diasSinVender,
        BigDecimal capitalInmovilizado
) {}
