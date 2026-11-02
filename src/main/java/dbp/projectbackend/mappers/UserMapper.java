package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.UserResponseDTO;
import dbp.projectbackend.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    UserResponseDTO toResponse(UserModel user);
}
