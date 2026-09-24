package dbp.projectbackend.services;

import dbp.projectbackend.dtos.ClientDTO;
import dbp.projectbackend.dtos.ClientResponseDTO;
import dbp.projectbackend.exceptions.DataIntegrityViolationException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.ClientModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.repositories.ClientRepository;
import dbp.projectbackend.repositories.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Transactional
    public ClientResponseDTO createClient(Long enterpriseId, ClientDTO dto) {
        EnterpriseModel assignedEnterprise = enterpriseRepository.findById(enterpriseId)
            .orElseThrow(() -> new ResourceNotFoundException("Empresa con id "+enterpriseId+" no encontrada."));

        if (clientRepository.existsByEmpresaIdAndDocumento(enterpriseId, dto.documento())) {
            throw new DataIntegrityViolationException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        ClientModel newClient = new ClientModel(dto.documento(), dto.nombre(), assignedEnterprise);
        newClient.setTelefono(dto.telefono());
        newClient.setCorreo(dto.correo());
        newClient.setDireccion(dto.direccion());

        return toDTO(clientRepository.save(newClient));
    }

    public ClientResponseDTO getClientById(Long id) {
        return toDTO(findClient(id));
    }

    public List<ClientResponseDTO> getClientsByEnterprise(Long enterpriseId) {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new ResourceNotFoundException("Empresa con id "+enterpriseId+" no encontrada.");
        }
        return clientRepository.findByEmpresaId(enterpriseId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ClientResponseDTO updateClient(Long id, ClientDTO dto) {
        ClientModel client = findClient(id);

        // si cambia el documento, validar que no choque con otro cliente de la misma empresa
        if (!client.getDocumento().equals(dto.documento())
                && clientRepository.existsByEmpresaIdAndDocumento(client.getEmpresa().getId(), dto.documento())) {
            throw new DataIntegrityViolationException("Ya existe un cliente con documento "+dto.documento()+" en esta empresa.");
        }

        client.setDocumento(dto.documento());
        client.setNombre(dto.nombre());
        client.setTelefono(dto.telefono());
        client.setCorreo(dto.correo());
        client.setDireccion(dto.direccion());

        return toDTO(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.delete(findClient(id));
    }

    // helper methods
    private ClientModel findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id "+id+" no encontrado."));
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
