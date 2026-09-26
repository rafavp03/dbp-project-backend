package dbp.projectbackend.services;

import dbp.projectbackend.dtos.SupplierDTO;
import dbp.projectbackend.dtos.SupplierResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.SupplierModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final EnterpriseRepository enterpriseRepository;

    // Un proveedor puede compartirse entre varias empresas: si ya existe (mismo RUC),
    // solo se vincula a la empresa actual en vez de duplicarlo.
    @Transactional
    public SupplierResponseDTO createSupplier(UserModel currentUser, SupplierDTO dto) {
        EnterpriseModel empresa = findEmpresa(currentUser);

        SupplierModel supplier = supplierRepository.findByRuc(dto.ruc())
                .orElseGet(() -> {
                    SupplierModel nuevo = new SupplierModel(dto.ruc(), dto.razonSocial());
                    nuevo.setTelefono(dto.telefono());
                    nuevo.setCorreo(dto.correo());
                    return supplierRepository.save(nuevo);
                });

        if (!empresa.getProveedores().contains(supplier)) {
            empresa.addSupplier(supplier);
        }

        return toDTO(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(UserModel currentUser, Long id) {
        return toDTO(findOwnedSupplier(currentUser, id));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> getSuppliersByEnterprise(UserModel currentUser) {
        return supplierRepository.findByEmpresasId(currentUser.getEmpresa().getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    private EnterpriseModel findEmpresa(UserModel currentUser) {
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

    private SupplierResponseDTO toDTO(SupplierModel supplier) {
        return new SupplierResponseDTO(
                supplier.getId(),
                supplier.getRuc(),
                supplier.getRazonSocial(),
                supplier.getTelefono(),
                supplier.getCorreo()
        );
    }
}
