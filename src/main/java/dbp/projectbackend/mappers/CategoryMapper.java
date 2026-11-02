package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.CategoryDTO;
import dbp.projectbackend.dtos.response.CategoryResponseDTO;
import dbp.projectbackend.models.CategoryModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class)
public interface CategoryMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    CategoryResponseDTO toResponse(CategoryModel category);

    /**
     * Copia los datos editables del DTO a la entidad. El id, la empresa, el estado y la
     * fecha de registro no vienen en el request, por eso se ignoran.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntity(CategoryDTO dto, @MappingTarget CategoryModel category);
}
