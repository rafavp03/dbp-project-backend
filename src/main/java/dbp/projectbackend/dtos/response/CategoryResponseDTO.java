package dbp.projectbackend.dtos.response;

public record CategoryResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        Long empresaId
) {}