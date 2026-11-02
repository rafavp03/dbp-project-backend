package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.EnterpriseDTO;
import dbp.projectbackend.dtos.response.EnterpriseResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.EnterpriseMapper;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EnterpriseService {
    private final EnterpriseRepository repository;
    private final EnterpriseMapper enterpriseMapper;

    @Transactional
    public EnterpriseResponseDTO createEnterprise(EnterpriseDTO dto) {
        EnterpriseModel newEnterprise = new EnterpriseModel(dto.ruc(), dto.razonSocial());
        return enterpriseMapper.toResponse(repository.save(newEnterprise));
    }

    @Transactional(readOnly = true)
    public EnterpriseResponseDTO getEnterpriseById(Long id) {
        return enterpriseMapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con id " + id + " no encontrada.")));
    }
}
