package dbp.projectbackend.dtos.response;

public record AuthResponseDTO(
        String token,
        String refreshToken,
        UserResponseDTO user
) {}
