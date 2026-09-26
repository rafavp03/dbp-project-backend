package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {}
