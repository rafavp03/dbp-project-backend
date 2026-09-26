package dbp.projectbackend.dtos.response;

public record AuthResponseDTO(
        String token,
        UserResponseDTO user
) {}
