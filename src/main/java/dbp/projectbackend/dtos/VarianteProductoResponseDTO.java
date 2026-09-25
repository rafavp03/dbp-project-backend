package dbp.projectbackend.dtos;

public record VarianteProductoResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        String talla,
        String color,
        String sku,
        Integer stock,
        Integer stockMinimo,
        Boolean activo,
        Boolean stockBajo
) {}
