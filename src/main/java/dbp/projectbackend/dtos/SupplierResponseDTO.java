package dbp.projectbackend.dtos;

public record SupplierResponseDTO(
        Long id,
        String ruc,
        String razonSocial,
        String telefono,
        String correo
) {}