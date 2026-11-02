package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.EnterpriseResponseDTO;
import dbp.projectbackend.models.EnterpriseModel;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface EnterpriseMapper {

    EnterpriseResponseDTO toResponse(EnterpriseModel empresa);
}
