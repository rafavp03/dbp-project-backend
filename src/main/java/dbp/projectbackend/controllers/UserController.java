package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.UserDTO;
import dbp.projectbackend.dtos.UserResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getCurrentUser(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@AuthenticationPrincipal UserModel admin, @Valid @RequestBody UserDTO dto) {
        UserResponseDTO newUser = service.createUser(admin, dto);
        return ResponseEntity
                .created(URI.create("user/"+newUser.id()))
                .body(newUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsersOfEnterprise(@AuthenticationPrincipal UserModel admin) {
        return ResponseEntity.ok(service.getUsersOfEnterprise(admin));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserModel admin, @PathVariable Long id) {
        service.deleteUser(admin, id);
        return ResponseEntity.noContent().build();
    }
}
