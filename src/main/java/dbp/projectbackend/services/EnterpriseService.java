package dbp.projectbackend.services;

import dbp.projectbackend.dtos.EnterpriseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EnterpriseService {
    private final EnterpriseRepository repository;
    private final ModelMapper modelMapper;

    public EnterpriseModel createEnterprise(EnterpriseDTO dto) {
        return repository.save(modelMapper.map(dto, EnterpriseModel.class));
    }

    public EnterpriseDTO getEnterpriseById(Long id) {
        return modelMapper.map(
                repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa con id "+id+" no encontrada.")),
                EnterpriseDTO.class
        );
    }
}
