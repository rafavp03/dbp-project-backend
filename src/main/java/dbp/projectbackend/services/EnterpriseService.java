package dbp.projectbackend.services;

import dbp.projectbackend.dtos.EnterpriseDTO;
import dbp.projectbackend.models.Enterprise;
import dbp.projectbackend.repositories.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EnterpriseService {
    private final EnterpriseRepository repository;
    private final ModelMapper modelMapper;

    public Enterprise createEnterprise(EnterpriseDTO dto) {
        return repository.save(modelMapper.map(dto, Enterprise.class));
    }

    public EnterpriseDTO getEnterpriseById(Long id) {
        return modelMapper.map(repository.findById(id), EnterpriseDTO.class);
    }
}
