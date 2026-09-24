package dbp.projectbackend.dtos;

// DTO de salida: incluye el id (el front lo necesita para editar/eliminar) y el id de la empresa
public record ClientResponseDTO(
        Long id,
        String documento,
        String nombre,
        Integer telefono,
        String correo,
        String direccion,
        Long empresaId
) {}
