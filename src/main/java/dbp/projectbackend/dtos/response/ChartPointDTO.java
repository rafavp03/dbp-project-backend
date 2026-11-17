package dbp.projectbackend.dtos.response;

import java.math.BigDecimal;

public record ChartPointDTO(
        String etiqueta,
        BigDecimal valor
) {}
