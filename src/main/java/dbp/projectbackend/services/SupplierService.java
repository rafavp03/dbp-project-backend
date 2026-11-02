package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.SupplierDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.SupplierResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.SupplierMapper;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.SupplierModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponseDTO createSupplier(UserModel currentUser, SupplierDTO dto) {
        EnterpriseModel empresa = findEnterprise(currentUser);

        SupplierModel supplier = supplierRepository.findByRuc(dto.ruc())
                .orElseGet(() -> {
                    SupplierModel nuevo = new SupplierModel(dto.ruc(), dto.razonSocial());
                    supplierMapper.updateEntity(dto, nuevo);
                    return supplierRepository.save(nuevo);
                });

        if (!empresa.getProveedores().contains(supplier)) {
            empresa.addSupplier(supplier);
        }

        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(UserModel currentUser, Long id) {
        return supplierMapper.toResponse(findOwnedSupplier(currentUser, id));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<SupplierResponseDTO> getSuppliersByEnterprise(UserModel currentUser, Pageable pageable) {
        return PageResponseDTO.from(
                supplierRepository.findByEmpresasId(currentUser.getEmpresa().getId(), pageable)
                        .map(supplierMapper::toResponse));
    }

    private EnterpriseModel findEnterprise(UserModel currentUser) {
        Long empresaId = currentUser.getEmpresa().getId();
        return enterpriseRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con id " + empresaId + " no encontrada."));
    }

    private SupplierModel findOwnedSupplier(UserModel currentUser, Long id) {
        SupplierModel supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con id " + id + " no encontrado."));

        boolean perteneceAEmpresa = supplier.getEmpresas().stream()
                .anyMatch(e -> e.getId().equals(currentUser.getEmpresa().getId()));

        if (!perteneceAEmpresa) {
            throw new ResourceNotFoundException("Proveedor con id " + id + " no encontrado.");
        }
        return supplier;
    }
}
