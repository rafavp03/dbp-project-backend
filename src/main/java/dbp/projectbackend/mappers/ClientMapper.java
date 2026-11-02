package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.ClientDTO;
import dbp.projectbackend.dtos.response.ClientResponseDTO;
import dbp.projectbackend.models.ClientModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class)
public interface ClientMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    ClientResponseDTO toResponse(ClientModel client);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntity(ClientDTO dto, @MappingTarget ClientModel client);
}
