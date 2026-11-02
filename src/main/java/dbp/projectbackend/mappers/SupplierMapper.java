package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.SupplierDTO;
import dbp.projectbackend.dtos.response.SupplierResponseDTO;
import dbp.projectbackend.models.SupplierModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class)
public interface SupplierMapper {

    SupplierResponseDTO toResponse(SupplierModel supplier);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntity(SupplierDTO dto, @MappingTarget SupplierModel supplier);
}
