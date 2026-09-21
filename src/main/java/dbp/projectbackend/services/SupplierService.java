package dbp.projectbackend.services;

import dbp.projectbackend.dtos.SupplierDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.SupplierModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SupplierModel createSupplier(Long enterpriseId, SupplierDTO dto) {
        SupplierModel newSupplier = supplierRepository.findByRuc(dto.ruc())
            .orElseGet(() -> {
                SupplierModel mappedSupplier = modelMapper.map(dto, SupplierModel.class);
                return supplierRepository.save(mappedSupplier);
            });

        EnterpriseModel assignedEnterprise = enterpriseRepository.findById(enterpriseId)
            .orElseThrow(() -> new ResourceNotFoundException("Empresa con id "+enterpriseId+" no encontrada."));

        if (!assignedEnterprise.getProveedores().contains(newSupplier)) {
            assignedEnterprise.addSupplier(newSupplier);
        }

        return newSupplier;
    }

    public SupplierDTO getSupplierById(Long id) {
        return modelMapper.map(
                supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proveedor con id"+id+" no encontrado.")),
                SupplierDTO.class
        );
    }
}
