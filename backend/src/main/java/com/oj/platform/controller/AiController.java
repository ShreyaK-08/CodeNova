package com.oj.platform.controller;

import com.oj.platform.dto.AiChatRequest;
import com.oj.platform.dto.AiChatResponse;
import com.oj.platform.dto.UserAiContextDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "student";

        AiChatResponse response = aiService.chat(request, userId, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/context")
    public ResponseEntity<UserAiContextDto> getContext(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserAiContextDto context = aiService.getUserAiContext(currentUser.getId());
        return ResponseEntity.ok(context);
    }
}
