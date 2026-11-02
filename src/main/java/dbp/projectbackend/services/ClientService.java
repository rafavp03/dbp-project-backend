package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ClientDTO;
import dbp.projectbackend.dtos.response.ClientResponseDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.ClientMapper;
import dbp.projectbackend.models.ClientModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional
    public ClientResponseDTO createClient(UserModel currentUser, ClientDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (clientRepository.existsByEmpresaIdAndDocumento(empresa.getId(), dto.documento())) {
            throw new DuplicateResourceException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        ClientModel newClient = new ClientModel(dto.documento(), dto.nombre(), empresa);
        clientMapper.updateEntity(dto, newClient);

        return clientMapper.toResponse(clientRepository.save(newClient));
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(UserModel currentUser, Long id) {
        return clientMapper.toResponse(findOwnedClient(currentUser, id));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ClientResponseDTO> getClientsByEnterprise(UserModel currentUser, Pageable pageable) {
        return PageResponseDTO.from(
                clientRepository.findByEmpresaId(currentUser.getEmpresa().getId(), pageable)
                        .map(clientMapper::toResponse));
    }

    @Transactional
    public ClientResponseDTO updateClient(UserModel currentUser, Long id, ClientDTO dto) {
        ClientModel client = findOwnedClient(currentUser, id);

        if (!client.getDocumento().equals(dto.documento())
                && clientRepository.existsByEmpresaIdAndDocumento(client.getEmpresa().getId(), dto.documento())) {
            throw new DuplicateResourceException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        clientMapper.updateEntity(dto, client);

        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(UserModel currentUser, Long id) {
        clientRepository.delete(findOwnedClient(currentUser, id));
    }

    private ClientModel findOwnedClient(UserModel currentUser, Long id) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id "+id+" no encontrado."));

        if (!client.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Cliente con id "+id+" no encontrado.");
        }
        return client;
    }
}
