package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.UserDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.UserResponseDTO;
import dbp.projectbackend.events.UserDeletedEvent;
import dbp.projectbackend.events.UserRegisteredEvent;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.UserMapper;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDTO createUser(UserModel admin, UserDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El email "+dto.email()+" ya esta registrado.");
        }

        UserModel newUser = new UserModel(
                dto.nombre(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.role(),
                admin.getEmpresa()
        );

        UserModel guardado = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisteredEvent(
                this, guardado.getNombre(), guardado.getEmail(), admin.getEmpresa().getRazonSocial(), guardado.getRole()));

        return userMapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(UserModel user) {
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getUsersOfEnterprise(UserModel admin, Pageable pageable) {
        return PageResponseDTO.from(
                userRepository.findByEmpresaId(admin.getEmpresa().getId(), pageable)
                        .map(userMapper::toResponse));
    }

    @Transactional
    public void deleteUser(UserModel admin, Long id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id "+id+" no encontrado."));

        if (!user.getEmpresa().getId().equals(admin.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Usuario con id "+id+" no encontrado.");
        }
        if (user.getId().equals(admin.getId())) {
            throw new InvalidOperationException("No puedes eliminar tu propio usuario.");
        }

        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeletedEvent(
                this, user.getNombre(), user.getEmail(), admin.getEmpresa().getRazonSocial(), admin.getNombre()));
    }
}
