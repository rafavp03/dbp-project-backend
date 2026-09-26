package dbp.projectbackend.dtos.response.report;

public record StockBajoDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        int stockMinimo
) {}
