package dbp.projectbackend.services;

import dbp.projectbackend.dtos.AuthResponseDTO;
import dbp.projectbackend.dtos.LoginDTO;
import dbp.projectbackend.dtos.RegisterDTO;
import dbp.projectbackend.events.UsuarioRegistradoEvent;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.Role;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponseDTO register(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El email "+dto.email()+" ya esta registrado.");
        }

        EnterpriseModel empresa = enterpriseRepository.findById(dto.enterpriseId())
            .orElseThrow(() -> new ResourceNotFoundException("Empresa con id "+dto.enterpriseId()+" no encontrada."));

        if (userRepository.existsByEmpresaId(empresa.getId())) {
            throw new InvalidOperationException("La empresa ya tiene administrador. Pidele que te cree un usuario.");
        }

        UserModel newUser = new UserModel(
                dto.nombre(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                Role.ADMIN,
                empresa
        );
        userRepository.save(newUser);
        eventPublisher.publishEvent(new UsuarioRegistradoEvent(
                this, newUser.getNombre(), newUser.getEmail(), empresa.getRazonSocial(), Role.ADMIN));

        return new AuthResponseDTO(jwtService.generateToken(newUser), userService.toDTO(newUser));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );
        UserModel user = (UserModel) authentication.getPrincipal();

        return new AuthResponseDTO(jwtService.generateToken(user), userService.toDTO(user));
    }
}
