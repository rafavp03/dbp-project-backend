package dbp.projectbackend.dtos.response;

public record ClientResponseDTO(
        Long id,
        String documento,
        String nombre,
        String telefono,
        String correo,
        String direccion,
        Long empresaId
) {}
