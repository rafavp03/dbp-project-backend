package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.SalesOrderLineResponseDTO;
import dbp.projectbackend.dtos.response.SalesOrderResponseDTO;
import dbp.projectbackend.models.SalesOrderLineModel;
import dbp.projectbackend.models.SalesOrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface SalesOrderMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNombre", source = "cliente.nombre")
    SalesOrderResponseDTO toResponse(SalesOrderModel orden);

    @Mapping(target = "varianteId", source = "variante.id")
    @Mapping(target = "varianteDescripcion", source = "variante.fullName")
    SalesOrderLineResponseDTO toLineResponse(SalesOrderLineModel detalle);
}
