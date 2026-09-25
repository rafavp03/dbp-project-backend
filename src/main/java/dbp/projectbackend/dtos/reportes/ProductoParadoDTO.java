package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

// Variante con stock que no se vende hace varios dias
public record ProductoParadoDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        LocalDate ultimaVenta,             // null si nunca se vendio
        long diasSinVender,
        BigDecimal capitalInmovilizado     // stock * costo: dinero "parado" en esa mercaderia
) {}
