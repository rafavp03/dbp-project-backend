package dbp.projectbackend.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisualPointDTO(
        String etiqueta,
        Object valor,
        String nota
) {}
