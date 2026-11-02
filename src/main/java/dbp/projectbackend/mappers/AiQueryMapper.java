package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.AiQueryResponseDTO;
import dbp.projectbackend.models.AiQueryModel;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface AiQueryMapper {

    AiQueryResponseDTO toResponse(AiQueryModel consulta);
}
