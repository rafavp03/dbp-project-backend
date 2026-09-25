package dbp.projectbackend.dtos;

public record ClientResponseDTO(
        Long id,
        String documento,
        String nombre,
        Integer telefono,
        String correo,
        String direccion,
        Long empresaId
) {}
