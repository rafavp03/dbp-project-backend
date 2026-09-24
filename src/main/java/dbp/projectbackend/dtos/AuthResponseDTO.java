package dbp.projectbackend.dtos;

public record AuthResponseDTO(
        String token,
        UserResponseDTO user
) {}
