package dbp.projectbackend.services;

import dbp.projectbackend.dtos.ClientDTO;
import dbp.projectbackend.dtos.ClientResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.exceptions.UnauthorizedException;
import dbp.projectbackend.models.ClientModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClientService {
    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponseDTO createClient(UserModel currentUser, ClientDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (clientRepository.existsByEmpresaIdAndDocumento(empresa.getId(), dto.documento())) {
            throw new DuplicateResourceException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        ClientModel newClient = new ClientModel(dto.documento(), dto.nombre(), empresa);
        newClient.setTelefono(dto.telefono());
        newClient.setCorreo(dto.correo());
        newClient.setDireccion(dto.direccion());

        return toDTO(clientRepository.save(newClient));
    }

    public ClientResponseDTO getClientById(UserModel currentUser, Long id) {
        return toDTO(findOwnedClient(currentUser, id));
    }

    public List<ClientResponseDTO> getClientsByEnterprise(UserModel currentUser) {
        return clientRepository.findByEmpresaId(currentUser.getEmpresa().getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ClientResponseDTO updateClient(UserModel currentUser, Long id, ClientDTO dto) {
        ClientModel client = findOwnedClient(currentUser, id);

        if (!client.getDocumento().equals(dto.documento())
                && clientRepository.existsByEmpresaIdAndDocumento(client.getEmpresa().getId(), dto.documento())) {
            throw new DuplicateResourceException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        client.setDocumento(dto.documento());
        client.setNombre(dto.nombre());
        client.setTelefono(dto.telefono());
        client.setCorreo(dto.correo());
        client.setDireccion(dto.direccion());

        return toDTO(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(UserModel currentUser, Long id) {
        clientRepository.delete(findOwnedClient(currentUser, id));
    }

    // helper methods
    private ClientModel findOwnedClient(UserModel currentUser, Long id) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id "+id+" no encontrado."));

        if (!client.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new UnauthorizedException("No tienes acceso a este cliente.");
        }
        return client;
    }

    private ClientResponseDTO toDTO(ClientModel client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getDocumento(),
                client.getNombre(),
                client.getTelefono(),
                client.getCorreo(),
                client.getDireccion(),
                client.getEmpresa().getId()
        );
    }
}