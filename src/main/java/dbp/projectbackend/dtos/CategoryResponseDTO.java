package dbp.projectbackend.dtos;

public record CategoryResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        Long empresaId
) {}