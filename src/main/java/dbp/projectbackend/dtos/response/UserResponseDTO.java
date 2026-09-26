package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.Role;

public record UserResponseDTO(
        Long id,
        String nombre,
        String email,
        Role role,
        Long empresaId
) {}
