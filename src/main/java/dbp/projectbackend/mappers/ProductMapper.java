package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.ProductDTO;
import dbp.projectbackend.dtos.request.ProductPatchDTO;
import dbp.projectbackend.dtos.response.ProductResponseDTO;
import dbp.projectbackend.models.ProductModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class)
public interface ProductMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "stockTotal", source = "totalStock")
    ProductResponseDTO toResponse(ProductModel product);

    /**
     * PUT: reemplaza los datos del producto. La categoría la resuelve el service (debe pertenecer
     * a la empresa) y la unidad de medida solo cambia si viene informada, porque es obligatoria.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "unidadMedida", source = "unidadMedida",
            conditionExpression = "java(dto.unidadMedida() != null && !dto.unidadMedida().isBlank())",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductDTO dto, @MappingTarget ProductModel product);

    /**
     * PATCH: solo copia los campos que llegaron con valor; los {@code null} se ignoran.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(ProductPatchDTO dto, @MappingTarget ProductModel product);
}
