package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.PurchaseOrderLineResponseDTO;
import dbp.projectbackend.dtos.response.PurchaseOrderResponseDTO;
import dbp.projectbackend.models.PurchaseOrderLineModel;
import dbp.projectbackend.models.PurchaseOrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface PurchaseOrderMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "proveedorId", source = "proveedor.id")
    @Mapping(target = "proveedorRazonSocial", source = "proveedor.razonSocial")
    PurchaseOrderResponseDTO toResponse(PurchaseOrderModel orden);

    @Mapping(target = "varianteId", source = "variante.id")
    @Mapping(target = "varianteDescripcion", source = "variante.fullName")
    PurchaseOrderLineResponseDTO toLineResponse(PurchaseOrderLineModel detalle);
}
