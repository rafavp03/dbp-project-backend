package dbp.projectbackend.dtos.response;

public record SupplierResponseDTO(
        Long id,
        String ruc,
        String razonSocial,
        String telefono,
        String correo
) {}