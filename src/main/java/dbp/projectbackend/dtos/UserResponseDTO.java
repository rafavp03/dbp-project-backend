package dbp.projectbackend.dtos;

import dbp.projectbackend.models.Role;

public record UserResponseDTO(
        Long id,
        String nombre,
        String email,
        Role role,
        Long empresaId
) {}
