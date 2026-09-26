package dbp.projectbackend.dtos.response;

import java.time.LocalDateTime;

public record EnterpriseResponseDTO(
        Long id,
        String ruc,
        String razonSocial,
        LocalDateTime fechaRegistro
) {}