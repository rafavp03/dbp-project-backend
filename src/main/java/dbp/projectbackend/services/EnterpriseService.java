package dbp.projectbackend.services;

import dbp.projectbackend.dtos.EnterpriseDTO;
import dbp.projectbackend.dtos.EnterpriseResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EnterpriseService {
    private final EnterpriseRepository repository;

    @Transactional
    public EnterpriseResponseDTO createEnterprise(EnterpriseDTO dto) {
        EnterpriseModel newEnterprise = new EnterpriseModel(dto.ruc(), dto.razonSocial());
        return toDTO(repository.save(newEnterprise));
    }

    public EnterpriseResponseDTO getEnterpriseById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con id " + id + " no encontrada.")));
    }

    private EnterpriseResponseDTO toDTO(EnterpriseModel empresa) {
        return new EnterpriseResponseDTO(
                empresa.getId(),
                empresa.getRuc(),
                empresa.getRazonSocial(),
                empresa.getFechaRegistro()
        );
    }
}