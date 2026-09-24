package dbp.projectbackend.dtos;

import dbp.projectbackend.models.Role;

// DTO de salida: nunca incluye la contraseña
public record UserResponseDTO(
        Long id,
        String nombre,
        String email,
        Role role,
        Long empresaId
) {}
