package dbp.projectbackend.dtos.reportes;

public record StockBajoDTO(
        Long varianteId,
        String producto,
        String talla,
        String color,
        int stock,
        int stockMinimo
) {}
