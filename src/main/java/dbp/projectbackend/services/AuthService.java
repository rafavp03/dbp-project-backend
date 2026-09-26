package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.LoginDTO;
import dbp.projectbackend.dtos.request.RefreshTokenDTO;
import dbp.projectbackend.dtos.request.RegisterDTO;
import dbp.projectbackend.dtos.response.AuthResponseDTO;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.UserRegisteredEvent;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.exceptions.InvalidTokenException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.security.JwtService;
import io.jsonwebtoken.JwtException;
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

    private static final String REFRESH_INVALIDO = "El refresh token no es valido o ha expirado.";
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
        eventPublisher.publishEvent(new UserRegisteredEvent(
                this, newUser.getNombre(), newUser.getEmail(), empresa.getRazonSocial(), Role.ADMIN));

        return authResponse(newUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );
        UserModel user = (UserModel) authentication.getPrincipal();

        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refresh(RefreshTokenDTO dto) {
        UserModel user = userFromRefreshToken(dto.refreshToken());
        if (!jwtService.isRefreshTokenValid(dto.refreshToken(), user)) {
            throw new InvalidTokenException(REFRESH_INVALIDO);
        }
        return authResponse(user);
    }

    private UserModel userFromRefreshToken(String refreshToken) {
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (JwtException ex) {
            throw new InvalidTokenException(REFRESH_INVALIDO);
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(REFRESH_INVALIDO));
    }

    private AuthResponseDTO authResponse(UserModel user) {
        return new AuthResponseDTO(
                jwtService.generateToken(user),
                jwtService.generateRefreshToken(user),
                userService.toDTO(user));
    }
}
