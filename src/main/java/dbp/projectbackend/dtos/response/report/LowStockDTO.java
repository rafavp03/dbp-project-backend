package dbp.projectbackend.dtos.response.report;

public record LowStockDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        int stockMinimo
) {}
