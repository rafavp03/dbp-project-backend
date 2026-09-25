package dbp.projectbackend.services;

import dbp.projectbackend.dtos.UserDTO;
import dbp.projectbackend.dtos.UserResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        return toDTO(userRepository.save(newUser));
    }

    public UserResponseDTO getCurrentUser(UserModel user) {
        return toDTO(user);
    }

    public List<UserResponseDTO> getUsersOfEnterprise(UserModel admin) {
        return userRepository.findByEmpresaId(admin.getEmpresa().getId()).stream()
                .map(this::toDTO)
                .toList();
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
    }

    public UserResponseDTO toDTO(UserModel user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                user.getRole(),
                user.getEmpresa().getId()
        );
    }
}
