package com.example.application.controller;

import com.example.application.config.JwtAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String mechanism = authentication instanceof JwtAuthenticationToken ? "JWT" : "HTTP_BASIC";
        return ResponseEntity.ok(new ProfileResponse(authentication.getName(), roles, mechanism));
    }
}

record ProfileResponse(String username, List<String> roles, String authenticationMechanism) {}
