package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.StockMovementResponseDTO;
import dbp.projectbackend.models.StockMovementModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface StockMovementMapper {

    @Mapping(target = "varianteId", source = "variante.id")
    @Mapping(target = "varianteDescripcion", source = "variante.fullName")
    StockMovementResponseDTO toResponse(StockMovementModel movimiento);
}
