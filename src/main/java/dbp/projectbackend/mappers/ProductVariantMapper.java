package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.ProductVariantDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.models.ProductVariantModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class)
public interface ProductVariantMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "stockBajo", source = "lowStock")
    ProductVariantResponseDTO toResponse(ProductVariantModel variante);

    /**
     * Copia talla, color y SKU. El stock nunca se edita aquí (solo por movimientos de kardex)
     * y el stock mínimo solo cambia si viene informado.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "stockMinimo", source = "stockMinimo",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductVariantDTO dto, @MappingTarget ProductVariantModel variante);
}
