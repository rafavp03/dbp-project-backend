package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.QuestionDTO;
import dbp.projectbackend.dtos.response.AiQueryResponseDTO;
import dbp.projectbackend.dtos.response.AssistantAnswerResponseDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/assistant")
@PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
public class AssistantController {

    private final AssistantService service;

    @PostMapping
    public ResponseEntity<AssistantAnswerResponseDTO> ask(@AuthenticationPrincipal UserModel user,
                                                           @Valid @RequestBody QuestionDTO dto) {
        return ResponseEntity.ok(service.ask(user, dto.pregunta()));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponseDTO<AiQueryResponseDTO>> history(
            @AuthenticationPrincipal UserModel user,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.history(user, pageable));
    }
}
