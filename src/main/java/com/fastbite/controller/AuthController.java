package com.fastbite.controller;

import com.fastbite.dto.AuthResponse;
import com.fastbite.dto.LoginRequest;
import com.fastbite.dto.RegisterRequest;
import com.fastbite.model.User;
import com.fastbite.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().build()
                ));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                ));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String userId = (String) authentication.getPrincipal();
        return authService.getCurrentUser(userId)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("email", user.getEmail());
                    response.put("name", user.getName());
                    response.put("role", user.getRole());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }
}