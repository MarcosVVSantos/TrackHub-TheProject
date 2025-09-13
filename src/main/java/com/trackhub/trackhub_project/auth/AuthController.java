package com.trackhub.trackhub_project.auth;

import com.trackhub.trackhub_project.auth.dto.AuthRequest;
import com.trackhub.trackhub_project.auth.dto.AuthResponse;
import com.trackhub.trackhub_project.auth.dto.RegisterRequest;
import com.trackhub.trackhub_project.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
